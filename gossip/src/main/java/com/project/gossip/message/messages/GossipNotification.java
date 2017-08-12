package com.project.gossip.message.messages;


import java.nio.ByteBuffer;

import com.project.gossip.message.Message;

public class GossipNotification extends Message {

  private short messageId;
  private short datatype;
  private byte[] data;

  public GossipNotification(short size, short type, short messageId,
                            short datatype, byte[] data) throws Exception {
    this.messageId = messageId;
    this.datatype = datatype;
    this.data = data;

    super.setType(type);
    super.setSize(size);
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
  
  public ByteBuffer getByteBuffer() throws Exception {
    short size = super.getSize();
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.putShort(size);
    buffer.putShort(super.getType().getVal());
    buffer.putShort(messageId);
    buffer.putShort(datatype);
    buffer.put(data);
    return buffer;
  }
}
