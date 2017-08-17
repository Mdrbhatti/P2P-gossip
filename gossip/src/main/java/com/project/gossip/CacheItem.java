package com.project.gossip;

import com.project.gossip.message.messages.GossipAnnounce;

import java.nio.channels.SocketChannel;

public class CacheItem {
  private GossipAnnounce gossipAnnounceMsg;
  private short messageId;
  //socket channel who has sent this gossip Announce Msg
  private SocketChannel originPeer;

  public CacheItem(GossipAnnounce msg, short msgId,
                   SocketChannel originPeer) {
    this.gossipAnnounceMsg = msg;
    this.messageId = msgId;
    this.originPeer = originPeer;
  }

  public GossipAnnounce getGossipAnnounceMsg() {
    return gossipAnnounceMsg;
  }

  public short getMessageId() {
    return messageId;
  }

  public SocketChannel getOriginPeer() {
    return originPeer;
  }
}
