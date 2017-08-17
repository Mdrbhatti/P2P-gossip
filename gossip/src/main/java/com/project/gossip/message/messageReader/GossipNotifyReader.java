package com.project.gossip.message.messageReader;

import com.project.gossip.message.messages.GossipNotify;

import java.nio.ByteBuffer;

public class GossipNotifyReader {

  public static GossipNotify read(ByteBuffer header, ByteBuffer payload) {
    GossipNotify msg = null;

    try {
      //check for buffer underflow
      if (!header.hasRemaining() && !payload.hasRemaining()) {
        return null;
      }

      short size = header.getShort();
      short type = header.getShort();
      short reserved = payload.getShort();
      short datatype = payload.getShort();
      msg = new GossipNotify(size, type, reserved, datatype);
    } catch (Exception exp) {
      exp.printStackTrace();
      return null;
    }
    return msg;
  }
}
