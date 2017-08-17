package com.project.gossip.message.messages;

import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.Message;

import java.nio.ByteBuffer;

public class GossipValidation extends Message {

  private short messageId;
  private short reserved;

  public GossipValidation(short size, short type, short messageId,
                          short reserved) throws Exception {
    this.messageId = messageId;
    this.reserved = reserved;

    super.setSize(size);
    super.setType(type);
  }

  public short getMessageId() {
    return this.messageId;
  }

  public short getReserved() {
    return reserved;
  }

  public boolean isValid() {
    if ((reserved & 0x1) == 1) {
      return true;
    }
    else {
      return false;
    }
  }

  public ByteBuffer getByteBuffer() {
    try {
      short size = super.getSize();
      ByteBuffer buffer = ByteBuffer.allocate(size);
      buffer.putShort(size);
      buffer.putShort(super.getType().getVal());
      buffer.putShort(messageId);
      buffer.putShort(reserved);
      return buffer;
    } catch (Exception exp) {
      P2PLogger.error("Unable to create gossip validation bytebuffer");
      exp.printStackTrace();
      return null;
    }
  }
}
