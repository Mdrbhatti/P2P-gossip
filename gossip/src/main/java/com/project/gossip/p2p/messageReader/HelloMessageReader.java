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

      msg = new HelloMessage(size, type);

      buffer.clear();
    }
    catch(Exception exp){
      exp.printStackTrace();
      return null;
    }
    return msg;
  }
}
