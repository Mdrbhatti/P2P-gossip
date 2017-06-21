package com.project.gossip.p2p.messageReader;

import com.project.gossip.p2p.messages.HelloMessage;
import java.nio.ByteBuffer;

public class HelloMessageReader{

  public HelloMessageReader(){

  }

  public static HelloMessage read(ByteBuffer buffer){
    HelloMessage msg = null;

    try{
      //check for buffer underflow
      if(!buffer.hasRemaining()){
        return null;
      }

      buffer.flip();

      short size = buffer.getShort();
      short type = buffer.getShort();
      int peerID = buffer.getInt();

      msg = new HelloMessage(size, type, peerID);

      //check if there is some garbage data
      if(buffer.hasRemaining()){
        return null;
      }

      buffer.clear();
    }
    catch(Exception exp){
      exp.printStackTrace();
      return null;
    }
    return msg;
  }
}
