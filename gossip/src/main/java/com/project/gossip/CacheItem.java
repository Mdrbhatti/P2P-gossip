package com.project.gossip;

import com.project.gossip.message.messages.GossipAnnounce;

public class CacheItem {
  private GossipAnnounce gossipAnnounceMsg;
  private short messageId;
  
  public CacheItem(GossipAnnounce msg, short msgId) {
    gossipAnnounceMsg = msg;
    messageId = msgId;
  }

  public GossipAnnounce getGossipAnnounceMsg() {
    return gossipAnnounceMsg;
  }

  public short getMessageId() {
    return messageId;
  }
}
