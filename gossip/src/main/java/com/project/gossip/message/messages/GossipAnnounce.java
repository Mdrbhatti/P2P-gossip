package com.project.gossip.message.messages;

import com.project.gossip.message.Message;

import java.lang.Exception;
import java.nio.ByteBuffer;

public class GossipAnnounce extends Message{

  private byte ttl;
  private byte reserved;
  private short datatype;
  private byte [] data;

  public GossipAnnounce(short size, short type, byte ttl, byte reserved,
                        short datatype, byte [] data) throws Exception{
    this.ttl = ttl;
    this.reserved = reserved; 
    this.datatype = datatype;
    this.data = data;

    super.setSize(size);
    super.setType(type);
  }

  public byte getTtl(){
    return this.ttl;
  }
 
  public byte getReserved(){
    return this.reserved;
  }

  public short getDatatype(){
    return this.datatype;
  }

  public byte [] getData(){
    return this.data;
  }

  public ByteBuffer getByteBuffer() throws Exception{
    short size = super.getSize();
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.putShort(size);
    buffer.putShort(super.getType().getVal());
    buffer.put(ttl);
    buffer.put(reserved);
    buffer.putShort(datatype);
    buffer.put(data);
    return buffer;
  }
}
