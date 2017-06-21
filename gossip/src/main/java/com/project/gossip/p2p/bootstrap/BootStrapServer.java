package com.project.gossip.p2p;

import com.project.gossip.server.UdpServer;
import com.project.gossip.p2p.messageReader.HelloMessageReader;
import com.project.gossip.p2p.messages.HelloMessage;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;
import java.util.HashSet;

import java.lang.Short;
import java.lang.Integer;

public class BootStrapServer{
  /* TEST INTERACTIVELY: nc -u 127.0.0.1 54352 */
  private DatagramChannel serverSocket;
  private ByteBuffer buffer;
  private HashSet<String> peers = new HashSet<String>();

  public BootStrapServer(int port, String addr) throws Exception{
    this.serverSocket = new UdpServer(port, addr).getServerSocket();
    //TODO: remove hardcoding
    this.buffer = ByteBuffer.allocate(64*128);   
  }

  public void listen() throws IOException{
    while(true){
      InetSocketAddress clientAddress = (InetSocketAddress) 
                                        this.serverSocket.receive(buffer);

      //could happen
      if(clientAddress == null){
        System.out.println("Address was null");
        continue;
      }

      //validates a hello message
      HelloMessage msg = HelloMessageReader.read(buffer);
      if( msg == null){
        System.out.println("Invalid Message Recv");
      }
      else{
        String address = clientAddress.getAddress().getHostAddress();
        peers.add(address);
        System.out.println("Someone connected: "+ address);
        System.out.println("Sending peers list");

        //reply with peers list
        buffer.put(peers.toString().getBytes());
        buffer.flip();
        int bytesSent = serverSocket.send(buffer, clientAddress);
        buffer.clear();
      }
      
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
