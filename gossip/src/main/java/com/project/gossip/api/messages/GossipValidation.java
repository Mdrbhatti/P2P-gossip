package com.project.gossip.api.messages;

public class GossipValidation extends ApiMessage{

  private short messageId;
  private boolean valid;

  public GossipValidation(short size, short type, short messageId,
                        boolean valid){
    super(size, type);
    this.messageId = messageId;
    this.valid = valid;
  }

  public short getMessageId(){
    return this.messageId;
  }
 
  public boolean getValid(){
    return this.valid;
  }

}
