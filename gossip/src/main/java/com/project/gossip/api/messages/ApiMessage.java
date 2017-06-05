package com.project.gossip.api.messages;

import com.project.gossip.message.Message;

public class ApiMessage extends Message{

  public ApiMessage(short size, short type){
    super(size, type);
  }
}
