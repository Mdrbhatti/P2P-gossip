package com.project.gossip.p2p.messages;

import java.lang.Exception;

public class HelloMessage extends P2pMessage{

  private int peerID;

  public HelloMessage(short size, short type, int peerID) throws Exception{
    super(size, type);
    this.peerID = peerID;
  }

  public int getPeerID(){
    return this.peerID;
  }
}
