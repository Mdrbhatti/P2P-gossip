package com.project.gossip.message.messageReader;

import com.project.gossip.constants.Helpers;
import com.project.gossip.message.messages.PeerList;

import java.nio.ByteBuffer;
import java.util.ArrayList;


public class PeerListMessageReader {

  public PeerListMessageReader() {

  }

  public static PeerList read(ByteBuffer header, ByteBuffer payload) {
    PeerList msg = null;

    try {
      //check for buffer underflow
      if (!header.hasRemaining() && !payload.hasRemaining()) {
        return null;
      }

      short size = header.getShort();
      short type = header.getShort();
      short numOfPeers = payload.getShort();
      ArrayList<String> peerAddrList = new ArrayList<String>();

      for (int i = 0; i < numOfPeers; i++) {
        peerAddrList.add(Helpers.convertIntIpToString(payload.getInt()));
      }

      msg = new PeerList(size, type, numOfPeers, peerAddrList);
    } catch (Exception exp) {
      exp.printStackTrace();
      return null;
    }
    return msg;
  }

}

