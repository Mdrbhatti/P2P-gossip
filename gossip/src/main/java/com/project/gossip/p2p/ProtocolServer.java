package com.project.gossip.p2p;

import com.project.gossip.server.TcpServer;

import java.net.InetSocketAddress;

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

public class ProtocolServer{

  private ServerSocketChannel serverSocket;
  private List<SocketChannel> connectedPeers;

  //selector for new connections and read data events
  private Selector acceptAndReadSelector;

  //selector used for getting connections which are ready for writing
  private Selector writeSelector;

  //256KB buffer
  private final int BUFFER_SIZE = 256 * 1024;

  //single thread is servicing all channels, so no danger of conccurent access
  //same buffer used for reading and writing
  private ByteBuffer buffer = ByteBuffer.allocateDirect (BUFFER_SIZE); 

  public ProtocolServer(String addr, int port) throws Exception{

    this.serverSocket = new TcpServer(port, addr).getServerSocket();
    this.connectedPeers = new ArrayList<SocketChannel>();

    //create read and accept selector for event loop
    acceptAndReadSelector = Selector.open();

    //create selector for write events
    writeSelector = Selector.open();

    // Register server socket with the Selector for accept connection events
    serverSocket.register(acceptAndReadSelector, SelectionKey.OP_ACCEPT); 
  }

  /* This event loop handles read data events and new connections events
     whenever we have a new connection we also register it with writeSelector
     which is responsible for returning all connections which are ready
     for data to be written to them */

  public void acceptAndReadEventLoop() throws Exception{

    while(true){
      //wait for events
      int numOfChannelsReady = 0;
      try{
        numOfChannelsReady = acceptAndReadSelector.select(); 
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
        if(key.isAcceptable()){
          SocketChannel channel = acceptNewConnection(key);
          if(channel != null && channel.isConnected()){
            registerChannelWithSelectors(channel);
            connectedPeers.add(channel);
          }
        }
      }
    }
  }

  private void registerChannelWithSelectors(SocketChannel channel) 
                                              throws ClosedChannelException{

    //OP_READ : notify when there is data waiting to be read in channel
    channel.register(acceptAndReadSelector, SelectionKey.OP_READ);

    //OP_WRITE: notify when channel is ready for writing data 
    channel.register(writeSelector, SelectionKey.OP_WRITE);
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
 
    socketChannel.configureBlocking(true);

    return socketChannel;
  }

  public SocketChannel initiateConnection(String addr, int port) 
                                                          throws IOException {
    SocketChannel socketChannel = SocketChannel.open();
    socketChannel.configureBlocking(true);
  
    socketChannel.connect(new InetSocketAddress(addr, port));
     
    if(socketChannel != null && socketChannel.isConnected()){
      registerChannelWithSelectors(socketChannel);
      connectedPeers.add(socketChannel);
    }
   
    return socketChannel;
  }

  public List<SocketChannel> getConnectedPeers(){
    return connectedPeers;
  }
}

