package com.project.gossip.p2p.bootstrap;

import com.project.gossip.p2p.messages.HelloMessage;
import com.project.gossip.p2p.messages.PeerList;
import com.project.gossip.p2p.messageReader.PeerListMessageReader;
import com.project.gossip.message.MessageType;

import com.project.gossip.constants.Constants;
import com.project.gossip.logger.P2PLogger;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Level;

/*
Implement bootstrap client here
*/
public class BootStrapClient{
  private DatagramChannel channel;
  private InetSocketAddress bootStrapServerAddr;
  private ByteBuffer readBuffer;
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
      P2PLogger.log(Level.INFO, "Invalid Peer List Message Recvd");
      return null;
    }
    else{
      P2PLogger.log(Level.INFO, "got peer list");
      return peerListMsg.getPeerAddrList();   
    }
  }

}
