package com.project.gossip.bootstrap;

import com.project.gossip.server.UdpServer;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;

public class BootStrapServer{

  private DatagramChannel serverSocket;
  private ByteBuffer buffer;

  public BootStrapServer(int port, String addr) throws Exception{
    this.serverSocket = new UdpServer(port, addr).getServerSocket();
    //TODO: remove hardcoding
    this.buffer = ByteBuffer.allocate(64*128);   
  }

  public void listen() throws IOException{
    while(true){
      SocketAddress sa = this.serverSocket.receive(buffer);
      
      //could happen
      if(sa == null){
        continue;
      }
      System.out.println("someone connected "+ buffer.toString());
      buffer.flip();
      while(buffer.hasRemaining()){
        System.out.print((char) buffer.get());
      }
      buffer.clear();
    }
  }

  public static void main(String [] args) throws Exception{
    //just for testing, will be removed
    /*
      run bootstrap server
      for testing purpose use `cat README.md | nc -u 127.0.0.1 54352`
      to write to server. Currently server just outputs what is receives
    */
    BootStrapServer server = new BootStrapServer(54352, "127.0.0.1");
    server.listen();
  }
}
