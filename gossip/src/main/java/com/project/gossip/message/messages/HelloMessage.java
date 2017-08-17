package com.project.gossip.message.messages;

import com.project.gossip.constants.Constants;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.Message;
import com.project.gossip.message.MessageType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class HelloMessage extends Message {

  private String sourceIp = "";

  public HelloMessage(String sourceIp) throws Exception {
    super.setType(MessageType.GOSSIP_HELLO.getVal());
    short size = Constants.HEADER_LENGTH + 4;
    super.setSize(size);
    setIP(sourceIp);
  }

  public HelloMessage(short size, short type, String sourceIp) throws Exception {
    if (MessageType.GOSSIP_HELLO.getVal() != type) {
      throw new IllegalArgumentException();
    }

    setIP(sourceIp);
    super.setSize(size);
    super.setType(type);
  }

  public void setIP(String ip) {
    if (validIP(ip)) {
      this.sourceIp = ip;
    }
    else {
      throw new IllegalArgumentException("Invalid IP");
    }
  }

  public boolean validIP(String ip) {
    try {
      if (ip == null || ip.isEmpty()) {
        return false;
      }

      String[] parts = ip.split("\\.");
      if (parts.length != 4) {
        return false;
      }

      for (String s : parts) {
        int i = Integer.parseInt(s);
        if ((i < 0) || (i > 255)) {
          return false;
        }
      }
      if (ip.endsWith(".")) {
        return false;
      }

      return true;
    } catch (NumberFormatException nfe) {
      return false;
    }
  }

  private int convertIpStringToInt(String IP) throws UnknownHostException {
    return ByteBuffer.wrap(InetAddress.getByName(IP).getAddress()).getInt();
  }

  public ByteBuffer getByteBuffer() {
    try {
      short size = super.getSize();
      ByteBuffer buffer = ByteBuffer.allocate(size);
      buffer.putShort(size);
      buffer.putShort(super.getType().getVal());
      buffer.putInt(convertIpStringToInt(sourceIp));
      return buffer;
    } catch (Exception exp) {
      P2PLogger.error("Unable to create hello message bytebuffer");
      exp.printStackTrace();
      return null;
    }
  }

  public String getSourceIp() {
    return sourceIp;
  }
}
