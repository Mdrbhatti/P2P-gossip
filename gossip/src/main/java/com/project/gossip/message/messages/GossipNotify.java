package com.project.gossip.message.messages;

import com.project.gossip.message.Message;

import java.lang.Exception;
import java.nio.ByteBuffer;

public class GossipNotify extends Message {

  private short reserved;
  private short datatype;

  public GossipNotify(short size, short type, short reserved,
                      short datatype) throws Exception {
    this.reserved = reserved;
    this.datatype = datatype;

    super.setSize(size);
    super.setType(type);
  }

  public short getReserved() {
    return this.reserved;
  }

  public short getDatatype() {
    return this.datatype;
  }
  
  public ByteBuffer getByteBuffer() throws Exception {
    short size = super.getSize();
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.putShort(size);
    buffer.putShort(super.getType().getVal());
    buffer.putShort(reserved);
    buffer.putShort(datatype);
    return buffer;
  }
}
