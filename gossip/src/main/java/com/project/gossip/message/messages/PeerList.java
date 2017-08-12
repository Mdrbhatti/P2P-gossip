package com.project.gossip.message.messages;

import com.project.gossip.message.Message;
import com.project.gossip.message.MessageType;
import com.project.gossip.constants.*;

import java.lang.Exception;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class PeerList extends Message {

  private short numOfPeers;
  private ArrayList<String> peerAddrList;

  public PeerList(ArrayList<String> peers) throws Exception {
    this.numOfPeers = (short) peers.size();
    this.peerAddrList = new ArrayList<String>();
    for (String peerIP : peers) {
      this.peerAddrList.add(peerIP);
    }

    //header size in bytes
    short size = Constants.HEADER_LENGTH;
    //numOfPeers size in bytes
    size += 2;
    //peer list size in bytes
    size += (short) (peerAddrList.size() * 4);

    super.setSize(size);
    super.setType(MessageType.GOSSIP_PEER_LIST.getVal());

  }

  public PeerList(short size, short type, short numOfPeers,
                  ArrayList<String> peers) throws Exception {
    if ((MessageType.GOSSIP_PEER_LIST.getVal() != type) ||
        (numOfPeers != peers.size())) {
      throw new IllegalArgumentException();
    }

    this.numOfPeers = (short) peers.size();
    this.peerAddrList = peers;
    super.setSize(size);
    super.setType(type);

  }

  private int convertIpStringToInt(String IP) throws UnknownHostException {
    return ByteBuffer.wrap(InetAddress.getByName(IP).getAddress()).getInt();
  }

  public ArrayList<String> getPeerAddrList() {
    return this.peerAddrList;
  }

  public ByteBuffer getByteBuffer() throws Exception {
    short size = super.getSize();
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.putShort(size);
    buffer.putShort(super.getType().getVal());
    buffer.putShort(numOfPeers);
    for (String peerIp : peerAddrList) {
      buffer.putInt(convertIpStringToInt(peerIp));
    }

    return buffer;
  }
}
