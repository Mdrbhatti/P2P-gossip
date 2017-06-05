package com.project.gossip.api;

import com.project.gossip.server.TcpServer;

import java.util.Iterator;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.ByteBuffer; 

public class ProtocolServer{

  private ServerSocketChannel serverSocket;
  private Selector selector;

  //256KB buffer
  private final int BUFFER_SIZE = 256 * 1024;

  //single thread is servicing all channels, so no danger of conccurent access
  //same buffer used for reading and writing
  private ByteBuffer buffer = ByteBuffer.allocateDirect (BUFFER_SIZE); 

  public ProtocolServer(int port, String addr) throws Exception{
    //get tcp server socket 
    this.serverSocket = new TcpServer(port, addr).getServerSocket();

    //create selector for event loop
    selector = Selector.open();

    // Register server socket with the Selector for accept connection events
    serverSocket.register(selector, SelectionKey.OP_ACCEPT); 
  }

  public void eventLoop() throws Exception{

    while(true){
      //wait for events
      int numOfChannelsReady = 0;
      try{
        numOfChannelsReady = selector.select(); 
      }
      catch(IOException e){
        e.printStackTrace();
      }

      if(numOfChannelsReady == 0){
        //someother thread invoked wakeup() method of selector
        continue;
      }

      // Iterate over the set of selected keys
      Iterator it = selector.selectedKeys().iterator();
      while(it.hasNext()){
        SelectionKey key = (SelectionKey) it.next();

        //new incoming connection event
        if(key.isAcceptable()){
          this.accept(key);
        }
      }
    }
  }

  private int accept(SelectionKey key) throws IOException{
    //get the channel
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
 
    //accept conenction
    SocketChannel socketChannel = serverChannel.accept();
    if(socketChannel == null){
      //could happen because serverChannel is non-blocking
      return -1;
    }
  
    //set new channel non-blocking
    socketChannel.configureBlocking(false);

    //register new channel with selection
    //OP_READ : notify when there is data waiting to be read in channel
    //OP_WRITE: notify when channel is ready for writing data 
    //**not sure to add OP_WRITE**
    socketChannel.register(selector, SelectionKey.OP_READ);
    return 0;
  }
}

