package com.project.gossip.p2p.messages;

import com.project.gossip.message.MessageType;
import com.project.gossip.constants.Helpers;

import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;

public class HelloMessage extends P2pMessage{


  public HelloMessage(short size, short type) throws Exception{
    super(size, type);

    if(MessageType.GOSSIP_HELLO.getVal() != type){
      throw new IllegalArgumentException();
    }
  }

  public ByteBuffer getByteBuffer(){
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.putShort(this.size);
    buffer.putShort(this.type.getVal());
    return buffer;
  }
}
