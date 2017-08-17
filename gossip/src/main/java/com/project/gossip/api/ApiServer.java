package com.project.gossip.api;

import com.project.gossip.PeerKnowledgeBase;
import com.project.gossip.constants.Constants;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.MessageType;
import com.project.gossip.message.messageReader.GossipAnnounceReader;
import com.project.gossip.message.messageReader.GossipNotifyReader;
import com.project.gossip.message.messageReader.GossipValidationReader;
import com.project.gossip.message.messages.GossipAnnounce;
import com.project.gossip.message.messages.GossipNotify;
import com.project.gossip.message.messages.GossipValidation;
import com.project.gossip.server.TcpServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

//Api server maintains connections with Gossip Modules
public class ApiServer extends Thread {

  //256KB buffer
  private final int BUFFER_SIZE = 256 * 1024;
  //selector for new connections and read data events
  public Selector acceptAndReadSelector;
  private ServerSocketChannel serverSocket;
  //buffer to read payload of a message
  private ByteBuffer payloadBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

  //header buffer
  private ByteBuffer headerBuffer = ByteBuffer.allocate(Constants.HEADER_LENGTH);

  public ApiServer(String apiServerAddr, int apiServerPort) throws Exception {

    this.serverSocket = new TcpServer(apiServerPort, apiServerAddr)
        .getServerSocket();

    //create read and accept selector for event loop
    acceptAndReadSelector = Selector.open();

    // Register server socket with the Selector for accept connection events
    serverSocket.register(acceptAndReadSelector, SelectionKey.OP_ACCEPT);
  }

  public void run() {
    P2PLogger.info("API Server started...");
    acceptAndReadEventLoop();
  }

  /*
  * Handles events for new connections, read events and connection termination
  * events from Gossip Modules
  */
  public void acceptAndReadEventLoop() {

    while (true) {
      //wait for events
      int numOfChannelsReady = 0;
      try {
        //select returns after every 5secs or when any channel fires an event
        numOfChannelsReady = acceptAndReadSelector.select(5000);
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (numOfChannelsReady == 0) {
        //someother thread invoked wakeup() method of selector
        continue;
      }

      // Iterate over the set of selected keys
      Iterator it = acceptAndReadSelector.selectedKeys().iterator();
      while (it.hasNext()) {
        SelectionKey key = (SelectionKey) it.next();

        //new incoming connection event
        if (key.isAcceptable()) {
          P2PLogger.info("Some Gossip Moudle sent a Connection Request " +
              "to API Server");
          SocketChannel channel = acceptNewConnection(key);

          if (channel != null && channel.isConnected()) {
            registerChannelWithSelectors(channel);
          }
        }
        //event fired when some channel sends data
        if (key.isReadable()) {
          SocketChannel socketChannel = (SocketChannel) key.channel();
          payloadBuffer.clear();
          headerBuffer.clear();

          int bytesRead = 0;
          try {
            //read the header
            bytesRead = socketChannel.read(headerBuffer);

            //read returns -1 when remote closes conn gracefully
            if (bytesRead == -1) {
              key.cancel();
              closeConnection(socketChannel);
            }
            else {

              //change the header buffer to read mode
              headerBuffer.flip();

              short size = headerBuffer.getShort();
              short type = headerBuffer.getShort();

              //message reader will also read the header, move the position of
              // buffer to 0 to allow re-reading it
              headerBuffer.rewind();

              //read the payload
              while (bytesRead != size) {
                bytesRead += socketChannel.read(payloadBuffer);
              }

              //change the payload buffer to read mode
              payloadBuffer.flip();

              if (MessageType.GOSSIP_ANNOUNCE.getVal() == type) {

                GossipAnnounce gossipAnnounceMsg =
                    GossipAnnounceReader.read
                        (headerBuffer, payloadBuffer);
                if (gossipAnnounceMsg != null) {
                  P2PLogger.info("Gossip Announce Msg Received from a Gossip" +
                      "Module");
                  ByteBuffer writeBuffer = gossipAnnounceMsg.getByteBuffer();
                  if (writeBuffer != null) {
                    writeBuffer.flip();
                    //send announce msg to all peers
                    PeerKnowledgeBase.sendBufferToAllPeers
                        (writeBuffer, "Gossip Announce Message");

                    //send announce msg to all modules who have previously
                    //registered for this datatype
                    PeerKnowledgeBase.sendNotificationToModulesWithNoValidationExpected(
                        gossipAnnounceMsg
                    );
                  }
                }
              }
              if (MessageType.GOSSIP_NOTIFY.getVal() == type) {
                GossipNotify gossipNotify =
                    GossipNotifyReader.read(headerBuffer,
                        payloadBuffer);
                if (gossipNotify != null) {
                  P2PLogger.info("Gossip Notify Msg Received from a Gossip " +
                      "Module");
                  PeerKnowledgeBase.addValidDatatype(gossipNotify.getDatatype(),
                      socketChannel);
                }
              }
              if (MessageType.GOSSIP_VALIDATION.getVal() == type) {
                GossipValidation gossipValidation =
                    GossipValidationReader.read(headerBuffer, payloadBuffer);
                if (gossipValidation != null) {
                  if (gossipValidation.getMessageId() != -1) {
                    P2PLogger.info("Gossip Validation Msg Received from a " +
                        "Gossip Module");
                    if (gossipValidation.isValid()) {
                      P2PLogger.info("Gossip Validation Msg Valid");
                      PeerKnowledgeBase.sendGossipAnnounce(gossipValidation);
                    }
                    else {
                      P2PLogger.info("Gossip Validation Msg Invalid");
                      PeerKnowledgeBase.removeCacheItem(gossipValidation.getMessageId());
                    }
                  }
                }
              }
            }
          } catch (IOException exp) {
            // conn closed by remote disgracefully
            key.cancel();
            closeConnection(socketChannel);
          }
        }
        // Remove key from selected set; it's been handled
        it.remove();
      }
    }

  }

  private void registerChannelWithSelectors(SocketChannel channel) {

    try {
      channel.register(acceptAndReadSelector, SelectionKey.OP_READ);
    } catch (ClosedChannelException exp) {
      P2PLogger.error("Unable to register Gossip Module with event loop");
      exp.printStackTrace();
    }
  }

  private SocketChannel acceptNewConnection(SelectionKey key) {

    //get the channel
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

    //accept conenction
    SocketChannel socketChannel = null;
    try {
      socketChannel = serverChannel.accept();
      if (socketChannel == null) {
        //could happen because serverChannel is non-blocking
        return null;
      }
      socketChannel.configureBlocking(false);
    } catch (Exception exp) {
      exp.printStackTrace();
    }
    return socketChannel;
  }

  public void closeConnection(SocketChannel channel) {
    try {
      P2PLogger.info("Connection to Gossip Module closed");
      synchronized (PeerKnowledgeBase.validDatatypes) {
        for (short datatype : PeerKnowledgeBase.validDatatypes.keySet()) {
          if (PeerKnowledgeBase.validDatatypes.get(datatype).contains(channel)) {
            if (PeerKnowledgeBase.validDatatypes.get(datatype).size() == 1) {
              PeerKnowledgeBase.validDatatypes.remove(datatype);
            }
            else {
              PeerKnowledgeBase.validDatatypes.get(datatype).remove(channel);
            }
          }
        }
      }
      channel.close();
    } catch (IOException exp) {
      exp.printStackTrace();
    }
  }
}

