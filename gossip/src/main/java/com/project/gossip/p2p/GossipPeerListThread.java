package com.project.gossip.p2p;

import com.project.gossip.message.messages.PeerList;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

/*
* This thread is used by the peer to send it's list of connected peers to
* every other peer. Other peers will learn new peers from this information
* and open new connections to them
* */
public class GossipPeerListThread extends Thread {

    //hold an instance of protocol server to query list of connected peers
    private ProtocolServer protocolServer;


    public void run(){
      while(true){
          try{
              //wait 10 seconds
              Thread.sleep(10000);

              //get list of all connected peers
              HashMap<String, SocketChannel> connectedPeers =
                      PeerKnowledgeBase.connectedPeers;


              PeerList peerListMsg = new PeerList(PeerKnowledgeBase.getConnectedPeerIPs());
              ByteBuffer writeBuffer = peerListMsg.getByteBuffer();
              writeBuffer.flip();
              PeerKnowledgeBase.sendBufferToAllPeers(writeBuffer, "Peer List" +
                      " Message");
          }
          catch (Exception exp){
              exp.printStackTrace();
          }
      }
    }
}
