package com.project.gossip.message.messageReader;

import com.project.gossip.message.messages.GossipNotification;
import java.nio.ByteBuffer;

public class GossipNotificationReader {

  public static GossipNotification read(ByteBuffer buffer) {
    GossipNotification msg = null;

    try {
      //check for buffer underflow
      if (!buffer.hasRemaining()) {
        return null;
      }

      short size = buffer.getShort();
      short type = buffer.getShort();
      short messageId = buffer.getShort();
      short datatype = buffer.getShort();
      byte[] data = new byte[buffer.remaining()];
      buffer.get(data);
      msg = new GossipNotification(size, type, messageId, datatype, data);
    } catch (Exception exp) {
      exp.printStackTrace();
      return null;
    }
    return msg;
  }
}
