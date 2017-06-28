package com.project.gossip.p2p.messages;

import com.project.gossip.message.MessageType;
import com.project.gossip.constants.*;

import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;

public class HelloMessage extends P2pMessage{


  public HelloMessage() throws Exception{
    super.setType(MessageType.GOSSIP_HELLO.getVal());
    super.setSize(Constants.HEADER_LENGTH);
  }

  public HelloMessage(short size, short type) throws Exception{
    if(MessageType.GOSSIP_HELLO.getVal() != type){
      throw new IllegalArgumentException();
    }

    super.setSize(size);
    super.setType(type);
  }

  public ByteBuffer getByteBuffer(){
    short size = super.getSize();
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.putShort(size);
    buffer.putShort(super.getType().getVal());
    return buffer;
  }
}
