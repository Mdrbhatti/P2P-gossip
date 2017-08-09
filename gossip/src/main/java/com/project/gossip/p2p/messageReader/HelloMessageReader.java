package com.project.gossip.p2p.messageReader;

import com.project.gossip.constants.Helpers;
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

      short size = buffer.getShort();
      short type = buffer.getShort();
      String ip = Helpers.convertIntIpToString(buffer.getInt());

      msg = new HelloMessage(size, type, ip);

      buffer.clear();
    }
    catch(Exception exp){
      exp.printStackTrace();
      return null;
    }
    return msg;
  }
}
