package com.project.gossip.api.messages;

import java.lang.Exception;

public class GossipNotify extends ApiMessage{

  private byte reserved;
  private short datatype;

  public GossipNotify(short size, short type, byte reserved, 
                      short datatype) throws Exception{
    this.reserved = reserved; 
    this.datatype = datatype;

    super.setSize(size);
    super.setType(type);
  }

  public byte getReserved(){
    return this.reserved;
  }

  public short getDatatype(){
    return this.datatype;
  }
}
