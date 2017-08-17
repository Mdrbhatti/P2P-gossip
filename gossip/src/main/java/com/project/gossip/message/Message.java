package com.project.gossip.message;

import com.project.gossip.constants.Constants;
import com.project.gossip.logger.P2PLogger;

import java.io.Serializable;

public class Message implements Serializable {

  private static final long serialVersionUID = 1L;

  private short size;
  private MessageType type;

  private boolean validSize(short size) {
    if (size > Constants.MAX_MESSAGE_LENGTH ||
        size < Constants.MIN_MESSAGE_LENGTH) {
      return false;
    }
    return true;
  }

  public short getSize() {
    return this.size;
  }

  public void setSize(short size) {
    if (validSize(size)) {
      this.size = size;
    }
    else {
      P2PLogger.error("Message Size Invalid, min msg size is " +
          Constants.MIN_MESSAGE_LENGTH + " max msg size is " +
          Constants.MAX_MESSAGE_LENGTH);

      throw new IllegalArgumentException("invalid message size: " + size);
    }
  }

  public MessageType getType() {
    return this.type;
  }

  public void setType(short typeVal) throws Exception {
    //validity of type is checked inside getMessageType method
    this.type = MessageType.getMessageType(typeVal);
  }
}
