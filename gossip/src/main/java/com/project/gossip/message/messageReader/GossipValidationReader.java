package com.project.gossip.message.messageReader;

import com.project.gossip.message.messages.GossipValidation;

import java.nio.ByteBuffer;

public class GossipValidationReader {

  public static GossipValidation read(ByteBuffer header, ByteBuffer payload) {
    GossipValidation msg = null;

    try {
      //check for buffer underflow
      if (!header.hasRemaining() && !payload.hasRemaining()) {
        return null;
      }

      short size = header.getShort();
      short type = header.getShort();
      short messageId = payload.getShort();
      short reserved = payload.getShort();
      msg = new GossipValidation(size, type, messageId, reserved);
    } catch (Exception exp) {
      exp.printStackTrace();
      return null;
    }
    return msg;
  }
}
