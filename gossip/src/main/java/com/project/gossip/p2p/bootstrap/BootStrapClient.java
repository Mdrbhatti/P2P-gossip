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
  private ByteBuffer payloadBuffer;
  private ByteBuffer headerBuffer;
  private int clientPort;
  private String clientAddr;

  public BootStrapClient(String serverAddr, int serverPort, String clientAddr)
          throws
          Exception{

    this.clientAddr = clientAddr;
    this.clientPort = clientPort;

    this.channel = DatagramChannel.open();

    this.channel.socket().bind(new InetSocketAddress(clientAddr, 0));
    this.bootStrapServerAddr = new InetSocketAddress(serverAddr, serverPort);
    this.channel.connect(this.bootStrapServerAddr);
    //TODO: Fix hardcoding
    this.headerBuffer = ByteBuffer.allocate(Constants.HEADER_LENGTH);
    this.payloadBuffer = ByteBuffer.allocate(64*128);
  }

  public List<String> getPeersList() throws Exception{

    //send hello packet
    HelloMessage helloMsg = new HelloMessage(clientAddr);

    ByteBuffer writeBuffer = helloMsg.getByteBuffer();
    writeBuffer.flip();
    channel.write(writeBuffer);
    writeBuffer.clear();

    ByteBuffer [] arr = {headerBuffer, payloadBuffer};
    //read bytes from channel into header and payload buffer
    channel.read(arr);

    headerBuffer.flip();
    payloadBuffer.flip();
    PeerList peerListMsg = PeerListMessageReader.read(headerBuffer, payloadBuffer);
    headerBuffer.clear();
    payloadBuffer.clear();

    if(peerListMsg == null){
      System.out.println("Invalid Peer List Message Recvd");
      return null;
    }
    else{
      System.out.println("got peer list");
      return peerListMsg.getPeerAddrList();   
    }
  }

}
