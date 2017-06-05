package com.project.gossip.message;

import java.lang.IllegalArgumentException;
import com.project.gossip.contants.Constants;

public class Message{

  private short size;
  private MessageType type;

  public Message(short size, short typeVal){
    if(validSize(size)){
      this.size = size;
    }
    else{
     throw new IllegalArgumentException("invalid message size: "+size);
    }
    
    //validity of type is checked inside getMessageType method
    this.type = MessageType.getMessageType(typeVal);
  }

  private boolean validSize(short size){
    if(size > Constants.MAX_MESSAGE_LENGTH || 
       size < Constants.MIN_MESSAGE_LENGTH){
      return false;
    }
    return true;
  }

  public short getSize(){
    return this.size;
  }

  public MessageType getType(){
    return this.type;
  }

  public static void main(String [] args){
   
  }
}
