package com.project.gossip.message.messages;


import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.Message;

import java.nio.ByteBuffer;

public class GossipNotification extends Message {

  private short messageId;
  private short datatype;
  private byte[] data;

  public GossipNotification(short size, short type, short messageId,
                            short datatype, byte[] data) throws Exception {
    this.messageId = messageId;
    this.datatype = datatype;
    this.data = data;

    setSizeWithData(size, data);
    super.setType(type);
  }

  public void setSizeWithData(short size, byte[] data) {
    short validSize = (short) ((Short.BYTES * 4) + (data.length));
    if (validSize != size) {
      throw new IllegalArgumentException("Size of message must be equal to its length in bytes");
    }
    super.setSize(size);
    this.data = data;
  }

  public short getMessageId() {
    return this.messageId;
  }

  public short getDatatype() {
    return this.datatype;
  }

  public byte[] getData() {
    return this.data;
  }

  public ByteBuffer getByteBuffer() {
    try {
      short size = super.getSize();
      ByteBuffer buffer = ByteBuffer.allocate(size);
      buffer.putShort(size);
      buffer.putShort(super.getType().getVal());
      buffer.putShort(messageId);
      buffer.putShort(datatype);
      buffer.put(data);
      return buffer;
    } catch (Exception exp) {
      P2PLogger.error("Unable to create gossip notification bytebuffer");
      exp.printStackTrace();
      return null;
    }
  }
}
