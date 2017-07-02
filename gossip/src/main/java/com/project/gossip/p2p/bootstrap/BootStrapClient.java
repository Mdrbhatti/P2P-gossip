package com.project.gossip.p2p.bootstrap;

import com.project.gossip.p2p.messages.HelloMessage;
import com.project.gossip.p2p.messages.PeerList;
import com.project.gossip.p2p.messageReader.PeerListMessageReader;
import com.project.gossip.message.MessageType;

import com.project.gossip.constants.Constants;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.net.InetSocketAddress;
import java.util.List;

/*
Implement bootstrap client here
*/
public class BootStrapClient{
  private DatagramChannel channel;
  private InetSocketAddress bootStrapServerAddr;
  private ByteBuffer readBuffer;
  private int clientPort;
  private String clientAddr;

  public BootStrapClient(String serverAddr, int serverPort, String clientAddr,
                         int clientPort) throws Exception{

    this.clientAddr = clientAddr;
    this.clientPort = clientPort;

    this.channel = DatagramChannel.open();
    this.channel.socket().bind(new InetSocketAddress(clientAddr, clientPort));
    this.bootStrapServerAddr = new InetSocketAddress(serverAddr, serverPort);
    this.channel.connect(this.bootStrapServerAddr);
    //TODO: Fix hardcoding
    this.readBuffer = ByteBuffer.allocate(64*128);
  }

  public List<String> getPeersList() throws Exception{

    //send hello packet
    HelloMessage helloMsg = new HelloMessage();

    ByteBuffer writeBuffer = helloMsg.getByteBuffer();
    writeBuffer.flip();
    channel.write(writeBuffer);
    writeBuffer.clear();

    channel.read(readBuffer);
    PeerList peerListMsg = PeerListMessageReader.read(readBuffer);
    readBuffer.clear();

    if(peerListMsg == null){
      System.out.println("Invalid Peer List Message Recvd");
      return null;
    }
    else{
      System.out.println("got peer list");
      return peerListMsg.getPeerAddrList();   
    }
  }

  public static void main(String [] args) throws Exception{
    BootStrapClient c = new BootStrapClient("127.0.0.1", 54352, args[0], 
                                             Integer.parseInt(args[1]));
    for(String peerIP : c.getPeersList()){
      System.out.println(peerIP);
    }
  }
}
