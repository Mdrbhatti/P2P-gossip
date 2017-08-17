package com.project.gossip.message.messages;

import com.project.gossip.constants.*;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.Message;
import com.project.gossip.message.MessageType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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

    setSize(size, peers);
    super.setType(MessageType.GOSSIP_PEER_LIST.getVal());
  }

  public PeerList(short size, short type, short numOfPeers,
                  ArrayList<String> peers) throws Exception {

    if ((MessageType.GOSSIP_PEER_LIST.getVal() != type) ||
        (numOfPeers != peers.size())) {
      throw new IllegalArgumentException("Size of message must be equal to its length in bytes");
    }

    this.numOfPeers = (short) peers.size();
    this.peerAddrList = peers;
    setSize(size, peers);
    super.setType(type);
  }

  public void setSize(short size, ArrayList<String> peers) {
    short validSize = (short) (Short.BYTES * 3 + (peers.size() * 4));
    if (validSize != size) {
      throw new IllegalArgumentException("Size of message must be equal to its length in bytes");
    }
    super.setSize(size);
  }

  private int convertIpStringToInt(String IP) throws UnknownHostException {
    return ByteBuffer.wrap(InetAddress.getByName(IP).getAddress()).getInt();
  }

  public ArrayList<String> getPeerAddrList() {
    return this.peerAddrList;
  }

  public ByteBuffer getByteBuffer() {

    try {
      short size = super.getSize();
      ByteBuffer buffer = ByteBuffer.allocate(size);
      buffer.putShort(size);
      buffer.putShort(super.getType().getVal());
      buffer.putShort(numOfPeers);
      for (String peerIp : peerAddrList) {
        buffer.putInt(convertIpStringToInt(peerIp));
      }
      return buffer;
    } catch (Exception exp) {
      P2PLogger.error("Unable to create PeerList bytebuffer");
      exp.printStackTrace();
      return null;
    }
  }
}
