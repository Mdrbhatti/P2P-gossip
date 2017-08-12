package com.project.gossip.message;

import java.lang.IllegalArgumentException;

public enum MessageType {

  // API Message Types
  GOSSIP_ANNOUNCE((short) 500),
  GOSSIP_NOTIFY((short) 501),
  GOSSIP_NOTIFICATION((short) 502),
  GOSSIP_VALIDATION((short) 503),

  // Protocol Message Type
  GOSSIP_PROPAGATE((short) 504),
  GOSSIP_HELLO((short) 505),
  GOSSIP_PEER_LIST((short) 506);

  private short val;

  MessageType(short val) {
    this.val = val;
  }

  public short getVal() {
    return this.val;
  }

  public static MessageType getMessageType(short val) {
    for (MessageType type : MessageType.values()) {
      if (type.getVal() == val) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid message type " + val);
  }
}
