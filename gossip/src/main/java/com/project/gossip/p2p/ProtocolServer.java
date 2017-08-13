package com.project.gossip.p2p;

import com.project.gossip.Peer;
import com.project.gossip.PeerKnowledgeBase;
import com.project.gossip.constants.Constants;
import com.project.gossip.message.MessageType;
import com.project.gossip.message.messageReader.GossipAnnounceReader;
import com.project.gossip.message.messages.GossipAnnounce;
import com.project.gossip.p2p.bootstrap.BootStrapClient;
import com.project.gossip.message.messageReader.HelloMessageReader;
import com.project.gossip.message.messageReader.PeerListMessageReader;
import com.project.gossip.message.messages.HelloMessage;
import com.project.gossip.message.messages.PeerList;
import com.project.gossip.server.TcpServer;

import java.net.InetSocketAddress;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;

public class ProtocolServer extends Thread {

  private ServerSocketChannel serverSocket;

  private String peerAddr;

  private int protocolServerPort;

  private int degree;

  private BootStrapClient bootStrapClient;

  //selector for new connections and read data events
  public Selector acceptAndReadSelector;

  //256KB buffer
  private final int BUFFER_SIZE = 256 * 1024;

  //single thread is servicing all channels, so no danger of conccurent access
  //same buffer used for reading and writing
  private ByteBuffer payloadBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

  //header buffer
  private ByteBuffer headerBuffer = ByteBuffer.allocate(Constants.HEADER_LENGTH);

  public ProtocolServer(String protocolServerAddr, int protocolServerPort,
                        String bootStrapServerAddr, int bootStrapServerPort,
                        int degree)
      throws
      Exception {

    this.serverSocket = new TcpServer(protocolServerPort, protocolServerAddr)
        .getServerSocket();

    this.protocolServerPort = protocolServerPort;
    this.peerAddr = protocolServerAddr;
    this.degree = degree;

    this.bootStrapClient = new BootStrapClient(bootStrapServerAddr,
        bootStrapServerPort, protocolServerAddr);

    //create read and accept selector for event loop
    acceptAndReadSelector = Selector.open();

    // Register server socket with the Selector for accept connection events
    serverSocket.register(acceptAndReadSelector, SelectionKey.OP_ACCEPT);
  }

  public void run() {
    acceptAndReadEventLoop();
  }

  /* This event loop handles read data events and new connections events
     whenever we have a new connection we also register it with writeSelector
     which is responsible for returning all connections which are ready
     for data to be written to them */

  public void acceptAndReadEventLoop() {

    List<String> peerList = null;
    try {
      peerList = bootStrapClient.getPeersList();
    } catch (Exception exp) {
      System.out.println("BOOTSTRAPPING FAILED: Unable to get peers list from" +
          " bootstrap");
      System.out.println("Exiting...");
      System.exit(-1);
      exp.printStackTrace();
    }

    System.out.println("-------------------------------");
    System.out.println("Peer List Received from BootStrap Server");
    System.out.println("Peer List size: " + peerList.size());
    for (String peer : peerList) {
      System.out.println(peer);
    }
    System.out.println();
    for (String peer : peerList) {
      if (!peer.equals(peerAddr) && !PeerKnowledgeBase.connectedPeers
          .containsKey(peer)) {
        System.out.println("Trying to connect to: " + peer);
        initiateConnection(peer);
      }
    }
    System.out.println("-------------------------------");


    while (true) {
      //wait for events
      int numOfChannelsReady = 0;
      try {
        numOfChannelsReady = acceptAndReadSelector.select(5000);
        System.out.println("Size of Connected Peers: " + PeerKnowledgeBase
            .connectedPeers.size());
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
          System.out.println("-------------------------------");
          System.out.println("New Connection Request Received");
          SocketChannel channel = acceptNewConnection(key);

          if (channel != null && channel.isConnected()) {
            if (PeerKnowledgeBase.connectedPeers.size() == degree) {
              //close conn
              denyConnection(channel);
            } else {
              registerChannelWithSelectors(channel);
              sendHelloMessage(channel, peerAddr);
            }
          }
          System.out.println("-------------------------------");
        }
        //event fired when some channel sends data
        if (key.isReadable()) {
          SocketChannel socketChannel = (SocketChannel) key.channel();

          //clear the buffers
          headerBuffer.clear();
          payloadBuffer.clear();

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

              if (MessageType.GOSSIP_HELLO.getVal() == type) {

                if (PeerKnowledgeBase.connectedPeers.size() == degree) {
                  denyConnection(socketChannel);
                }
                else{
                  HelloMessage helloMessage = HelloMessageReader.read
                      (headerBuffer, payloadBuffer);
                  if (helloMessage != null) {
                    System.out.println("-------------------------------");
                    System.out.println("Hello Message Received from: " +
                        "" + helloMessage.getSourceIp());
                    PeerKnowledgeBase.connectedPeers.put(helloMessage
                            .getSourceIp(),
                        socketChannel);
                    System.out.println("Successfully Connected to: "
                        + helloMessage.getSourceIp());
                    System.out.println("-------------------------------");
                  }
                }
              }
              if (MessageType.GOSSIP_PEER_LIST.getVal() == type) {

                PeerList peerListMsg = PeerListMessageReader.read
                    (headerBuffer, payloadBuffer);
                if (peerListMsg != null) {
                  System.out.println("-------------------------------");
                  String neighborAddress = getPeerIpFromSocket(socketChannel);
                  System.out.println("PEERS LIST MESSAGE RECEIVED FROM " + neighborAddress);
                  for (String ip : peerListMsg.getPeerAddrList()) {
                    System.out.println(ip);
                  }
                  PeerKnowledgeBase.knownPeers.put(neighborAddress, peerListMsg.getPeerAddrList());
                  //maintain degree, open new connections if needed
                  if (PeerKnowledgeBase.connectedPeers.size() < degree) {
                    for (String peerIp : peerListMsg.getPeerAddrList()) {
                      if (!PeerKnowledgeBase.connectedPeers.containsKey(peerIp) &&
                          (!peerIp.equals(peerAddr)) &&
                          (PeerKnowledgeBase.getConnectedPeerIPs().size() < degree)) {
                        System.out.println("-------------------------------");
                        System.out.println("Trying to connect to: " + peerIp + " to maintain peer degree");
                        initiateConnection(peerIp);
                        System.out.println("-------------------------------");
                      }
                    }
                  }

                  System.out.println("-------------------------------");
                }
              }
              if (MessageType.GOSSIP_ANNOUNCE.getVal() == type) {
                GossipAnnounce gossipAnnounceMsg = GossipAnnounceReader.read
                        (headerBuffer, payloadBuffer);
                if (gossipAnnounceMsg != null) {
                  System.out.println("-------------------------------");
                  System.out.println("Gossip Announce Msg Received ");
                  short datatype = gossipAnnounceMsg.getDatatype();
                  if (PeerKnowledgeBase.containsDatatype(datatype)) {
                    PeerKnowledgeBase.sendNotificationToModules(datatype, gossipAnnounceMsg);
                  } else {
                    System.out.println("No Module has subscribed for Datatype " +
                        gossipAnnounceMsg.getDatatype() + " , dropping message");
                  }
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


  public void sendHelloMessage(SocketChannel channel, String sourceAddr) {
    try {
      HelloMessage helloMsg = new HelloMessage(sourceAddr);
      ByteBuffer writeBuffer = helloMsg.getByteBuffer();
      writeBuffer.flip();
      channel.write(writeBuffer);
      writeBuffer.clear();
    } catch (Exception exp) {
      System.out.println("Unable to send Hello Message");
      exp.printStackTrace();
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

  public void initiateConnection(String addr) {
    if (PeerKnowledgeBase.connectedPeers.size() < degree) {
      SocketChannel socketChannel = null;
      try {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(addr, protocolServerPort));
        while (!socketChannel.finishConnect()) {
        }
      } catch (IOException exp) {
        exp.printStackTrace();
        System.out.println("Unable to connect to peer " + addr);
      }

      if (socketChannel != null && socketChannel.isConnected()) {
        registerChannelWithSelectors(socketChannel);
        sendHelloMessage(socketChannel, peerAddr);
        try {
          Thread.sleep(2000);
        } catch (Exception exp) {
          exp.printStackTrace();
        }
      }
    } else {
      System.out.println("Conencted Peers equals to Degree, " +
          "discarding initiate connection request");
    }
  }

  public void closeConnection(SocketChannel channel) {
    try {
      String address = getPeerIpFromSocket(channel);
      //address is null when degree is violated and connection is closed
      //before adding any entry in connected peers list
      if (address == null) {
        System.out.println("Connection Request denied by peer");
      } else {
        System.out.println("Connection to peer " + address + " closed");
        PeerKnowledgeBase.connectedPeers.remove(address);
      }
      channel.close();
    } catch (IOException exp) {
      exp.printStackTrace();
    }
  }

  public void denyConnection(SocketChannel socketChannel){
    try {
      System.out.println("Incoming Connection Request Denied, " +
          "connected peers size " + PeerKnowledgeBase.connectedPeers.size() +
          " is equal to degree " + degree);
      socketChannel.close();
    } catch (Exception exp) {
      exp.printStackTrace();
    }
  }
  /*
   * Hashmap of connectedPeers contains <ip,socket> entries
   * when peers close their connection we can use the getAddress function of
   * socket, but during testing and dev phase we are running multiple peers
   * on same machine using different IPs from private address space, when
   * getAddress function is called on socket, it always returns 127.0.0.1
   * eventhough the IP was 127.0.0.2. */
  public String getPeerIpFromSocket(SocketChannel channel) {
    for (String key : PeerKnowledgeBase.connectedPeers.keySet()) {
      if (PeerKnowledgeBase.connectedPeers.get(key).equals(channel)) {
        return key;
      }
    }
    return null;
  }

  public int getDegree() {
    return degree;
  }

  public String myIp() {
    return peerAddr;
  }
}

