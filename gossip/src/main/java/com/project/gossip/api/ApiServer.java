package com.project.gossip.api;

import com.project.gossip.message.messageReader.GossipNotifyReader;
import com.project.gossip.message.messages.GossipAnnounce;
import com.project.gossip.message.messages.GossipNotify;
import com.project.gossip.constants.Constants;
import com.project.gossip.message.MessageType;
import com.project.gossip.PeerKnowledgeBase;
import com.project.gossip.server.TcpServer;
import com.project.gossip.message.messageReader.GossipAnnounceReader;

import java.util.Iterator;
import java.io.IOException;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;

public class ApiServer extends Thread {

  private ServerSocketChannel serverSocket;

  //selector for new connections and read data events
  public Selector acceptAndReadSelector;

  //256KB buffer
  private final int BUFFER_SIZE = 256 * 1024;

  //single thread is servicing all channels, so no danger of conccurent access
  //same buffer used for reading and writing
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
    try {
      acceptAndReadEventLoop();
    } catch (Exception exp) {
      exp.printStackTrace();
    }
  }

	/* This event loop handles read data events and new connections events
     whenever we have a new connection we also register it with writeSelector
		 which is responsible for returning all connections which are ready
		 for data to be written to them */

  public void acceptAndReadEventLoop() {

    while (true) {
      //wait for events
      int numOfChannelsReady = 0;
      try {
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
          System.out.println("Accept Event Triggered");
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
            } else {

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
                  System.out.println("-------------------------------");
                  System.out.println("Gossip " +
                      "Announce Msg Received ");
                  ByteBuffer writeBuffer = null;
                  try {
                    writeBuffer = gossipAnnounceMsg
                        .getByteBuffer();
                  } catch (Exception exp) {
                    System.out.println("Unable to get " +
                        "byte buffer from gossip " +
                        "announce object");
                    exp.printStackTrace();
                  }
                  if (writeBuffer != null) {
                    writeBuffer.flip();
                    PeerKnowledgeBase.sendBufferToAllPeers
                        (writeBuffer, "Gossip Announce " +
                            "Message");
                  }
                  System.out.println("-------------------------------");
                }
              }
              if (MessageType.GOSSIP_NOTIFY.getVal() == type) {
                GossipNotify gossipNotify =
                    GossipNotifyReader.read(headerBuffer,
                        payloadBuffer);
                if (gossipNotify != null) {
                  System.out.println("-------------------------------");
                  System.out.println("Gossip " +
                      "Notify Msg Received ");
                  PeerKnowledgeBase.addValidDatatype(gossipNotify.getDatatype(), socketChannel);
                  System.out.println("-------------------------------");
                }
              }
            }
          } catch (IOException e) {
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

    //OP_READ : notify when there is data waiting to be read in channel
    try {
      channel.register(acceptAndReadSelector, SelectionKey.OP_READ);
    } catch (ClosedChannelException exp) {
      exp.printStackTrace();
    }
    //OP_WRITE: notify when channel is ready for writing data
    //channel.register(writeSelector, SelectionKey.OP_WRITE);
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
      System.out.println("Connection to module closed");
      channel.close();
    } catch (IOException exp) {
      exp.printStackTrace();
    }
  }
}

