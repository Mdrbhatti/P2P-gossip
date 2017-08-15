package com.project.gossip.message.messages;

import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.Message;

import java.lang.Exception;
import java.nio.ByteBuffer;

public class GossipAnnounce extends Message {

  private byte ttl;
  private byte reserved;
  private short datatype;
  private byte[] data;

  public GossipAnnounce(short size, short type, byte ttl, byte reserved,
      short datatype, byte[] data) throws Exception {
    this.reserved = reserved;
    this.datatype = datatype;

    setSizeWithData(size, data);
    super.setType(type);

    setTtl(ttl);
  }

  public void setSizeWithData(short size, byte[] data) {
    short validSize = (short) ((Short.BYTES * 3) +(Byte.BYTES * 2) + (data.length));
    if (validSize != size){
      throw new IllegalArgumentException("Size of message must be equal to its length in bytes");
    }
    super.setSize(size);
    this.data = data;
  }

  public void checkMessageSize(byte [] data){

  }

  public void setTtl(byte ttl) {
    if(ttl < 0) {
      throw new IllegalArgumentException("TTL value must be >= 0");
    }
    else {
      this.ttl = ttl;
    }
  }

  public byte getTtl() {
    return this.ttl;
  }

  public byte getReserved() {
    return this.reserved;
  }

  public short getDatatype() {
    return this.datatype;
  }

  public byte[] getData() {
    return this.data;
  }

  public void decrementTTL(){
    this.ttl--;
  }

  public ByteBuffer getByteBuffer() {
    try{
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
    catch (Exception exp){
      P2PLogger.error("Unable to create gossip announce bytebuffer");
      exp.printStackTrace();
      return null;
    }
  }
}
