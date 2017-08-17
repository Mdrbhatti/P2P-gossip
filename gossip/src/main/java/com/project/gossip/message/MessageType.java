package com.project.gossip.message;

public enum MessageType {

  // API Message Types
  GOSSIP_ANNOUNCE((short) 500),
  GOSSIP_NOTIFY((short) 501),
  GOSSIP_NOTIFICATION((short) 502),
  GOSSIP_VALIDATION((short) 503),

  // Protocol Message Type
  GOSSIP_HELLO((short) 504),
  GOSSIP_PEER_LIST((short) 505);

  private short val;

  MessageType(short val) {
    this.val = val;
  }

  public static MessageType getMessageType(short val) {
    for (MessageType type : MessageType.values()) {
      if (type.getVal() == val) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid message type " + val);
  }

  public short getVal() {
    return this.val;
  }
}
