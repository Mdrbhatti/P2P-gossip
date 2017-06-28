package com.project.gossip.api.messages;

import java.lang.Exception;

public class GossipAnnounce extends ApiMessage{

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
}
