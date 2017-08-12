package com.project.gossip.message.messages;

import com.project.gossip.message.Message;


import java.lang.Exception;

public class GossipValidation extends Message{

  private short messageId;
  private boolean valid;

  public GossipValidation(short size, short type, short messageId,
                        boolean valid) throws Exception{
    this.messageId = messageId;
    this.valid = valid;
   
    super.setSize(size);
    super.setType(type);
  }

  public short getMessageId(){
    return this.messageId;
  }
 
  public boolean getValid(){
    return this.valid;
  }

}
