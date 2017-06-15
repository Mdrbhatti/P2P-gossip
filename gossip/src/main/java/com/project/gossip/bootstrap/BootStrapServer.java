package com.project.gossip.bootstrap;

import com.project.gossip.server.UdpServer;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Iterator;
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
	  HashMap<Integer,String> hashMap = new HashMap<Integer,String>(); 
    while(true){
      InetSocketAddress clientAddress = (InetSocketAddress) this.serverSocket.receive(buffer);
      int hash = clientAddress.hashCode();
      //could happen
      if(clientAddress == null){
        System.out.println("Address was null");
        continue;
      }
      String ip = clientAddress.getAddress().toString() + ":" + clientAddress.getPort();
      hashMap.put(hash, ip);
      System.out.println("Someone connected: " + ip);
      buffer.flip();
      while(buffer.hasRemaining()){
        System.out.print((char) buffer.get());
      }
      buffer.clear();
      System.out.print("Hashmap so far:\n--------------------------------\n");
      hashMap.forEach((k,v)-> System.out.println(k+", "+v));
      System.out.print("--------------------------------\n");
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
