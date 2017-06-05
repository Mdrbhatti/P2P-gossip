package com.project.gossip.p2p.messages;

public class HelloMessage extends P2pMessage{

  private String message;

  public HelloMessage(short size, short type, String message){
    super(size, type);
    this.message = message;
  }

  public String getMessage(){
    return this.message;
  }
}
