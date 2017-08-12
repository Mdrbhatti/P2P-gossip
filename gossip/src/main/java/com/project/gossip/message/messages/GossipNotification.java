package com.project.gossip.message.messages;


import com.project.gossip.message.Message;

public class GossipNotification extends Message{

  private short messageId;
  private short datatype;
  private byte [] data;

  public GossipNotification(short size, short type, short messageId, 
                            short datatype, byte [] data) throws Exception{
    this.messageId = messageId;
    this.datatype = datatype;
    this.data = data;

    super.setType(type);
    super.setSize(size);
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
