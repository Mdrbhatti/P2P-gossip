package com.project.gossip.p2p;

import com.project.gossip.PeerKnowledgeBase;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.messages.PeerList;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;

/*
* This thread is used by the peer to send the IPs of it's connected peers and
* the list of peer IPs learnt from the neighbors to all of its connected neighbors
* Peers can therefore learn about new peers and open connection to them.
* The maximum number of connections should not exceed the degree
* */

public class MaintainOverlay extends Thread {

  private long delay;
  //reference of protocol server
  private ProtocolServer protocolServer;

  public MaintainOverlay(long delay, ProtocolServer protocolServer) {
    this.delay = delay;
    this.protocolServer = protocolServer;
  }

  public void run() {
    while (true) {
      try {
        //wait 10 seconds
        Thread.sleep(delay);

        HashSet<String> peerIps = new HashSet<String>();
        //add ips of directly connected neighbors
        for (String peerIp : PeerKnowledgeBase.getConnectedPeerIPs()) {
          peerIps.add(peerIp);
        }

        for (String neighborPeer : PeerKnowledgeBase.knownPeers.keySet()) {
          for (String peerIp : PeerKnowledgeBase.knownPeers.get(neighborPeer)) {
            peerIps.add(peerIp);
          }
        }

        PeerList peerListMsg = new PeerList(new ArrayList<String>(peerIps));
        ByteBuffer writeBuffer = peerListMsg.getByteBuffer();
        writeBuffer.flip();
        PeerKnowledgeBase.sendBufferToAllPeers(writeBuffer, "Peer List" +
            " Message");

        //maintain degree, open new connections if needed
        if (PeerKnowledgeBase.connectedPeers.size() < protocolServer.getDegree()) {
          for (String neighborIP : PeerKnowledgeBase.knownPeers.keySet()) {
            for (String peerIP : PeerKnowledgeBase.knownPeers.get(neighborIP)) {
              if (!PeerKnowledgeBase.connectedPeers.containsKey(peerIP) &&
                  (!peerIP.equals(protocolServer.myIp())) &&
                  (PeerKnowledgeBase.getConnectedPeerIPs().size() < protocolServer.getDegree())) {
                P2PLogger.info("Opening a new connection to maintain " +
                    "degree");
                protocolServer.initiateConnection(peerIP);
              }
            }
          }
        }
      } catch (Exception exp) {
        exp.printStackTrace();
      }
    }
  }
}
