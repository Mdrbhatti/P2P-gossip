package com.project.gossip.p2p;

import com.project.gossip.p2p.messages.HelloMessage;
import com.project.gossip.message.MessageType;

import com.project.gossip.constants.Constants;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/*
Implement bootstrap client here
*/
public class BootStrapClient{
  private DatagramChannel channel;
  private InetSocketAddress bootStrapServerAddr;
  private ByteBuffer readBuffer;

  public BootStrapClient(String serverAddr, int serverPort)throws Exception{
    this.channel = DatagramChannel.open();
    this.bootStrapServerAddr = new InetSocketAddress(serverAddr, serverPort);
    this.channel.connect(this.bootStrapServerAddr);
    //TODO: Fix hardcoding
    this.readBuffer = ByteBuffer.allocate(64*128);
  }

  public ArrayList<String> getPeersList() throws Exception{
    //send hello packet

    short type = MessageType.GOSSIP_HELLO.getVal();
    short size = Constants.HEADER_LENGTH;

    HelloMessage msg = new HelloMessage(size, type);
    ByteBuffer writeBuffer = msg.getByteBuffer();
    writeBuffer.flip();
    this.channel.write(writeBuffer);
    writeBuffer.clear();

    this.channel.read(this.readBuffer);
    System.out.println(this.readBuffer.hasRemaining());
    this.readBuffer.clear();
    return null; 
  }

  public static void main(String [] args) throws Exception{
    BootStrapClient c = new BootStrapClient("127.0.0.1", 54352);
    c.getPeersList();
  }
}
