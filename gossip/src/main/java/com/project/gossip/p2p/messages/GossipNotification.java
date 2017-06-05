package com.project.gossip.p2p.messages;

public class GossipNotification extends P2pMessage{

  private short messageId;
  private short datatype;
  private byte [] data;

  public GossipNotification(short size, short type, short messageId,
                            short datatype, byte [] data){
    super(size, type);
    this.messageId = messageId;
    this.datatype = datatype;
    this.data = data;
  }

  public short getMessageId(){
    return this.messageId;
  }

  public short getDatatype(){
    return this.datatype;
  }

  public byte [] getData(){
    return this.data;
  }
}
