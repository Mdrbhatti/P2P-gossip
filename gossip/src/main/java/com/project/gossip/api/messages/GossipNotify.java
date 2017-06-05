package com.project.gossip.api.messages;

public class GossipNotify extends ApiMessage{

  private byte reserved;
  private short datatype;

  public GossipNotify(short size, short type, byte reserved, 
                        short datatype){
    super(size, type);
    this.reserved = reserved; 
    this.datatype = datatype;
  }

  public byte getReserved(){
    return this.reserved;
  }

  public short getDatatype(){
    return this.datatype;
  }
}
