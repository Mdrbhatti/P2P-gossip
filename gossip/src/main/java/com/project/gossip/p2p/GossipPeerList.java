package com.project.gossip.p2p;

import com.project.gossip.logger.P2PLogger;
import com.project.gossip.p2p.messages.PeerList;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/*
* This thread is used by the peer to send it's list of connected peers to
* every other peer. Other peers will learn new peers from this information
* and open new connections to them
* */
public class GossipPeerList extends Thread {

    //hold an instance of protocol server to query list of connected peers
    private ProtocolServer protocolServer;

    public GossipPeerList(ProtocolServer protocolServer){
        this.protocolServer = protocolServer;
    }

    public void run(){
      while(true){
          try{
              //wait 10 seconds
              Thread.sleep(10000);

              //get list of all connected peers
              HashMap<String, SocketChannel> connectedPeers = protocolServer
                      .getConnectedPeers();

              ArrayList<String> peers = new ArrayList<String>();

              for(String peerIp: connectedPeers.keySet()){
                  peers.add(peerIp);
              }
              PeerList peerListMsg = new PeerList(peers);
              ByteBuffer writeBuffer = peerListMsg.getByteBuffer();

              //send list of connected peers to each neighbouring peer
              for(String neighborIp: peers){
                P2PLogger.log(Level.INFO, "Sending peer list to: "+neighborIp);
                  writeBuffer.flip();
                  SocketChannel channel = connectedPeers.get(neighborIp);
                  if(channel.isConnected()) {
                      while (writeBuffer.hasRemaining()) {
                          channel.write(writeBuffer);
                      }
                  }
              }
          }
          catch (Exception exp){
              exp.printStackTrace();
          }
      }
    }
}
