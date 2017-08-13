package com.project.gossip.p2p.bootstrap;

import com.project.gossip.message.messages.HelloMessage;
import com.project.gossip.message.messages.PeerList;
import com.project.gossip.message.messageReader.PeerListMessageReader;

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
public class BootStrapClient {
  private DatagramChannel channel;
  private InetSocketAddress bootStrapServerAddr;
  private ByteBuffer payloadBuffer;
  private ByteBuffer headerBuffer;
  private int clientPort;
  private String clientAddr;

  public BootStrapClient(String serverAddr, int serverPort, String clientAddr)
      throws
      Exception {

    this.clientAddr = clientAddr;
    this.clientPort = clientPort;

    this.channel = DatagramChannel.open();

    this.channel.socket().bind(new InetSocketAddress(clientAddr, 0));
    this.bootStrapServerAddr = new InetSocketAddress(serverAddr, serverPort);
    this.channel.connect(this.bootStrapServerAddr);
    //TODO: Fix hardcoding
    this.headerBuffer = ByteBuffer.allocate(Constants.HEADER_LENGTH);
    this.payloadBuffer = ByteBuffer.allocate(64 * 128);
  }

  public List<String> getPeersList(){

    try{
      //send hello packet
      HelloMessage helloMsg = new HelloMessage(clientAddr);

      ByteBuffer writeBuffer = helloMsg.getByteBuffer();
      writeBuffer.flip();
      channel.write(writeBuffer);
      writeBuffer.clear();

      ByteBuffer[] arr = {headerBuffer, payloadBuffer};
      //read bytes from channel into header and payload buffer
      channel.read(arr);

      headerBuffer.flip();
      payloadBuffer.flip();
      PeerList peerListMsg = PeerListMessageReader.read(headerBuffer, payloadBuffer);
      headerBuffer.clear();
      payloadBuffer.clear();

      if (peerListMsg == null) {
        P2PLogger.log(Level.INFO, "Invalid Peer List Message Recvd");
        return null;
      }
      return peerListMsg.getPeerAddrList();
    }
    catch (Exception exp){
      System.out.println("Unable to get peer list from bootstrap server");
      exp.printStackTrace();
      return null;
    }
  }
}
