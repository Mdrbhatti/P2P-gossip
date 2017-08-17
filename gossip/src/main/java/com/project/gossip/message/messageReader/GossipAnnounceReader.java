package com.project.gossip.message.messageReader;

import com.project.gossip.message.messages.GossipAnnounce;

import java.nio.ByteBuffer;

public class GossipAnnounceReader {

  public static GossipAnnounce read(ByteBuffer header, ByteBuffer payload) {
    GossipAnnounce msg = null;

    try {
      //check for buffer underflow
      if (!header.hasRemaining() && !payload.hasRemaining()) {
        return null;
      }

      short size = header.getShort();
      short type = header.getShort();
      byte ttl = payload.get();
      byte reserved = payload.get();
      short datatype = payload.getShort();
      byte[] data = new byte[payload.remaining()];
      payload.get(data);
      msg = new GossipAnnounce(size, type, ttl, reserved, datatype, data);
    } catch (Exception exp) {
      exp.printStackTrace();
      return null;
    }
    return msg;
  }
}
