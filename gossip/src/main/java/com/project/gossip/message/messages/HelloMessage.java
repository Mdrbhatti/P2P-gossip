package com.project.gossip.message.messages;

import com.project.gossip.message.Message;
import com.project.gossip.message.MessageType;
import com.project.gossip.constants.*;

import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class HelloMessage extends Message {

  String sourceIp = "";

  public HelloMessage(String sourceIp) throws Exception {
    super.setType(MessageType.GOSSIP_HELLO.getVal());
    short size = Constants.HEADER_LENGTH + 4;
    super.setSize(size);
    this.sourceIp = sourceIp;
  }

  public HelloMessage(short size, short type, String sourceIp) throws Exception {
    if (MessageType.GOSSIP_HELLO.getVal() != type) {
      throw new IllegalArgumentException();
    }

    this.sourceIp = sourceIp;
    super.setSize(size);
    super.setType(type);
  }

  private int convertIpStringToInt(String IP) throws UnknownHostException {
    return ByteBuffer.wrap(InetAddress.getByName(IP).getAddress()).getInt();
  }

  public ByteBuffer getByteBuffer() throws Exception {
    short size = super.getSize();
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.putShort(size);
    buffer.putShort(super.getType().getVal());
    buffer.putInt(convertIpStringToInt(sourceIp));
    return buffer;
  }

  public String getSourceIp() {
    return sourceIp;
  }
}
