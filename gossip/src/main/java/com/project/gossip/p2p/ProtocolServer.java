package com.project.gossip.p2p;

import com.project.gossip.message.MessageType;
import com.project.gossip.p2p.bootstrap.BootStrapClient;
import com.project.gossip.p2p.messageReader.HelloMessageReader;
import com.project.gossip.p2p.messageReader.PeerListMessageReader;
import com.project.gossip.p2p.messages.HelloMessage;
import com.project.gossip.p2p.messages.PeerList;
import com.project.gossip.server.TcpServer;

import java.lang.reflect.Array;
import java.net.InetSocketAddress;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;

public class ProtocolServer extends Thread{

  private ServerSocketChannel serverSocket;
  private String peerAddr;

  private BootStrapClient bootStrapClient;

  // key is ip address
  private HashMap<String, SocketChannel> connectedPeers;

  //selector for new connections and read data events
  public Selector acceptAndReadSelector;

  //selector used for getting connections which are ready for writing
  private Selector writeSelector;

  //256KB buffer
  private final int BUFFER_SIZE = 256 * 1024;

  //single thread is servicing all channels, so no danger of conccurent access
  //same buffer used for reading and writing
  private ByteBuffer buffer = ByteBuffer.allocateDirect (BUFFER_SIZE); 

  public ProtocolServer(String protocolServerAddr, int protocolServerPort,
                        String bootStrapServerAddr, int bootStrapServerPort)
          throws
          Exception{

    this.serverSocket = new TcpServer(protocolServerPort, protocolServerAddr)
            .getServerSocket();

    this.peerAddr = protocolServerAddr;

    this.bootStrapClient = new BootStrapClient(bootStrapServerAddr,
            bootStrapServerPort, protocolServerAddr);

    this.connectedPeers = new HashMap<String, SocketChannel>();

    //create read and accept selector for event loop
    acceptAndReadSelector = Selector.open();

    //create selector for write events
    writeSelector = Selector.open();

    // Register server socket with the Selector for accept connection events
    serverSocket.register(acceptAndReadSelector, SelectionKey.OP_ACCEPT );
  }

  public void run(){
    try{
      acceptAndReadEventLoop();
    }
    catch (Exception exp){
      exp.printStackTrace();
    }
  }

  /* This event loop handles read data events and new connections events
     whenever we have a new connection we also register it with writeSelector
     which is responsible for returning all connections which are ready
     for data to be written to them */

  public void acceptAndReadEventLoop() throws Exception{

    List<String> peerList = bootStrapClient.getPeersList();

    for (String peer : peerList) {
      if (!peer.equals(peerAddr) && !connectedPeers.containsKey(peer)) {
        System.out.println("Trying to connect to: " + peer);

        SocketChannel channel = initiateConnection(peer);
        if (channel != null) {
          sendHelloMessage(channel, peerAddr);
        }
      }
    }

    while(true){
      //wait for events
      int numOfChannelsReady = 0;
      try{
        numOfChannelsReady = acceptAndReadSelector.select(5000);
        System.out.println("Size of Connected Peers: "+connectedPeers.size());

      }
      catch(IOException e){
        e.printStackTrace();
      }

      if(numOfChannelsReady == 0){
        //someother thread invoked wakeup() method of selector
        continue;
      }

      // Iterate over the set of selected keys
      Iterator it = acceptAndReadSelector.selectedKeys().iterator();
      while(it.hasNext()){
        SelectionKey key = (SelectionKey) it.next();

        //new incoming connection event
        if(key.isAcceptable()) {
          System.out.println("Accept Event Triggered");
          SocketChannel channel = acceptNewConnection(key);
          InetSocketAddress remoteAddr = (InetSocketAddress) channel.socket()
                  .getRemoteSocketAddress();
          if (channel != null && channel.isConnected()) {
              registerChannelWithSelectors(channel);
              sendHelloMessage(channel, peerAddr);
          }
        }
        //event fired when some channel sends data
        if(key.isReadable()){
          System.out.println("READ EVENT");
          SocketChannel socketChannel = (SocketChannel) key.channel();
          this.buffer.clear();

          int numOfBytesRead=0;
          try {
            while((numOfBytesRead = socketChannel.read(this.buffer)) > 0){
              System.out.println("Bytes recvd: "+numOfBytesRead);
            }
            //read mode
            this.buffer.flip();
            short size = buffer.getShort();
            short type = buffer.getShort();
            if(MessageType.GOSSIP_HELLO.getVal() == type){
              System.out.println("Something of type hello");
              this.buffer.rewind();
              System.out.println("Buffer position "+this.buffer.position());

              HelloMessage helloMessage = HelloMessageReader.read(this.buffer);
              if(helloMessage!=null){
                System.out.println("-------------------------------");
                System.out.println("Hello Message Received from: " +
                        ""+helloMessage.getSourceIp());
                connectedPeers.put(helloMessage.getSourceIp(), socketChannel);
                System.out.println("Successfully Connected to: "
                        +helloMessage.getSourceIp());
                System.out.println("-------------------------------");
              }
            }
            if(MessageType.GOSSIP_PEER_LIST.getVal() == type){
              this.buffer.rewind();
              PeerList peerListMsg = PeerListMessageReader.read(this.buffer);
              if(peerListMsg!=null){
                System.out.println("-------------------------------");
                System.out.println("RECV FOLLOWING PEERS FROM NEIGHBOR");
                for(String ip: peerListMsg.getPeerAddrList()){
                  System.out.println(ip);
                }
                System.out.println("-------------------------------");
              }
            }



          } catch (IOException e) {
            // conn closed by remote disgracefully
            key.cancel();
            socketChannel.close();
            connectedPeers.remove(socketChannel.socket().getInetAddress()
                    .getHostAddress());
          }

          //read returns -1 when remote closes conn gracefully
          if (numOfBytesRead == -1) {
            key.channel().close();
            key.cancel();
            System.out.println("SOCKET CLOSED BY REMOTE HOST");
            connectedPeers.remove(socketChannel.socket().getInetAddress()
                    .getHostAddress());
          }
        }
        // Remove key from selected set; it's been handled
        it.remove( );
      }
    }
  }



  public void sendHelloMessage(SocketChannel channel, String sourceAddr)
          throws Exception {
    HelloMessage helloMsg = new HelloMessage(sourceAddr);

    ByteBuffer writeBuffer = helloMsg.getByteBuffer();
    writeBuffer.flip();
    channel.write(writeBuffer);
    writeBuffer.clear();
  }

  private void registerChannelWithSelectors(SocketChannel channel) 
                                              throws ClosedChannelException{

    //OP_READ : notify when there is data waiting to be read in channel
    channel.register(acceptAndReadSelector, SelectionKey.OP_READ);

    //OP_WRITE: notify when channel is ready for writing data
    //channel.register(writeSelector, SelectionKey.OP_WRITE);
  }

  private SocketChannel acceptNewConnection(SelectionKey key) 
                                                          throws IOException{

    //get the channel
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
 
    //accept conenction
    SocketChannel socketChannel = serverChannel.accept();
    if(socketChannel == null){
      //could happen because serverChannel is non-blocking
      return null;
    }

    socketChannel.configureBlocking(false);
    return socketChannel;
  }

  public SocketChannel initiateConnection(String addr) throws Exception {
    SocketChannel socketChannel = SocketChannel.open();
    socketChannel.configureBlocking(false);

    socketChannel.connect(new InetSocketAddress(addr, 6001));

    while (!socketChannel.finishConnect()) {}

    if(socketChannel != null && socketChannel.isConnected()){
      registerChannelWithSelectors(socketChannel);
    }
    return socketChannel;
  }

  public HashMap<String,SocketChannel> getConnectedPeers(){
    return connectedPeers;
  }
}

