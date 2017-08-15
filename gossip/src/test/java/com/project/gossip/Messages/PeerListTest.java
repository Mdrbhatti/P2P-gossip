package com.project.gossip.Messages;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.project.gossip.message.MessageType;
import com.project.gossip.message.messages.PeerList;

public class PeerListTest {
  static short size, type, numOfPeers;
  static ArrayList<String> peers = new ArrayList<String>();
  
  @BeforeClass
  public static void beforeClass() throws Exception{
    type = MessageType.GOSSIP_PEER_LIST.getVal();
    peers.add("123.2.2.1");
    peers.add("123.2.2.2");
    peers.add("123.2.2.3");
    numOfPeers = getNumPeers();
    size = getCorrectSize();
  }
  
  public static short getCorrectSize(){
    return (short) (Short.BYTES * 3 + (peers.size() * 4));
  }
  
  public static short getNumPeers(){
    return (short)peers.size();
  }
  
  @Test
  public void createValidPeerList() throws Exception{
    // Two ways of constructing PeerList
    PeerList lst = new PeerList(getCorrectSize(), type, getNumPeers(), peers);
    Assert.assertNotNull(lst);
    lst = new PeerList(peers);
    Assert.assertNotNull(lst);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void createInValidSizePeerList() throws Exception{
    // size mismatch
    PeerList lst = new PeerList((short)(size-1), type, getNumPeers(), peers);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void createInValidNumPeerList() throws Exception{
    // numOfPeers mismatch
    PeerList lst = new PeerList(getCorrectSize(), type, (short)(numOfPeers-1), peers);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void createInValidPeersPeerList() throws Exception{
    // peers mismatch
    peers.add("22.11.1.1");
    PeerList lst = new PeerList(getCorrectSize(), type, numOfPeers, peers);
  }

}
