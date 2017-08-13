package com.project.gossip.p2p;

import com.project.gossip.Peer;
import com.project.gossip.PeerKnowledgeBase;
import com.project.gossip.message.messages.PeerList;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/*
* This thread is used by the peer to send the IPs of it's connected peers and
* the list of peer IPs learnt from the neighbors to all of its connected neighbors
* Peers can therefore learn about new peers and open connection to them.
* The maximum number of connections should not exceed the degree
* */

public class MaintainOverlay extends Thread {

  public void run() {
    while (true) {
      try {
        //wait 10 seconds
        Thread.sleep(10000);

        HashSet<String> peerIps = new HashSet<String>();
        //add ips of directly connected neighbors
        for(String peerIp : PeerKnowledgeBase.getConnectedPeerIPs()){
          peerIps.add(peerIp);
        }

        for(String neighborPeer : PeerKnowledgeBase.knownPeers.keySet()){
          for(String peerIp : PeerKnowledgeBase.knownPeers.get(neighborPeer)){
            peerIps.add(peerIp);
          }
        }

        PeerList peerListMsg = new PeerList(new ArrayList<String>(peerIps));
        ByteBuffer writeBuffer = peerListMsg.getByteBuffer();
        writeBuffer.flip();
        PeerKnowledgeBase.sendBufferToAllPeers(writeBuffer, "Peer List" +
            " Message");

       /* //try to open connection to new peer IPs learnt from neighbors
        //to maintain degree
        if(PeerKnowledgeBase.getConnectedPeerIPs().size() < protocolServer.getDegree()){
          for(String neighborPeer : PeerKnowledgeBase.knownPeers.keySet()){
            for(String peerIp : PeerKnowledgeBase.knownPeers.get(neighborPeer)){
              if(!PeerKnowledgeBase.connectedPeers.containsKey(peerIp) &&
                  (!peerIp.equals(protocolServer.myIp())) &&
                  (PeerKnowledgeBase.getConnectedPeerIPs().size() < protocolServer.getDegree())){
                System.out.println("-------------------------------");
                System.out.println("Trying to connect to: " + peerIp +" to maintain peer degree");
                protocolServer.initiateConnection(peerIp);
                System.out.println("-------------------------------");
              }
            }
          }
        }*/
      } catch (Exception exp) {
        exp.printStackTrace();
      }
    }
  }
}
