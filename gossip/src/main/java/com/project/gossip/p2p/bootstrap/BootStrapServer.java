package com.project.gossip.p2p.bootstrap;

import com.project.gossip.server.UdpServer;
import com.project.gossip.p2p.messageReader.HelloMessageReader;
import com.project.gossip.p2p.messages.HelloMessage;
import com.project.gossip.p2p.messages.PeerList;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.ArrayList;

import java.lang.Short;
import java.lang.Integer;

public class BootStrapServer{
  private DatagramChannel serverSocket;
  private ByteBuffer readBuffer;
  private HashSet<String> peers = new HashSet<String>();

  public BootStrapServer(int port, String addr) throws Exception{
    this.serverSocket = new UdpServer(port, addr).getServerSocket();
    //TODO: remove hardcoding
    this.readBuffer = ByteBuffer.allocate(64*128);   
  }

  public void listen() throws Exception{
    while(true){
      InetSocketAddress clientAddress = (InetSocketAddress) 
                                        this.serverSocket.receive(readBuffer);

      //could happen
      if(clientAddress == null){
        System.out.println("Address was null");
        continue;
      }

      //validates a hello message
      HelloMessage msg = HelloMessageReader.read(readBuffer);
      readBuffer.clear();

      if( msg == null){
        System.out.println("Invalid Hello Message Recv");
      }
      else{
        String address = clientAddress.getAddress().getHostAddress();
        peers.add(address);

        System.out.println("Someone connected: "+ address);
        System.out.println("Sending peers list");

        //reply with peers list
        PeerList peerListMsg = new PeerList(new ArrayList<String>(peers));

        ByteBuffer writeBuffer = peerListMsg.getByteBuffer();
        writeBuffer.flip();
        int bytesSent = serverSocket.send(writeBuffer, clientAddress);
        writeBuffer.clear();
      }     
    }
  }

  public static void main(String [] args) throws Exception{

    BootStrapServer server = new BootStrapServer(6002, "127.0.0.1");
    server.listen();
  }
}
