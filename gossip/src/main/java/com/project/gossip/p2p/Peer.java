package com.project.gossip.p2p;

import com.project.gossip.p2p.bootstrap.BootStrapClient;

import java.lang.Exception;

public class Peer{

  private ProtocolServer protocolServer;
  private BootStrapClient bootstrapClient;
  private int degree;

  public Peer(int degree, String protocolServerAddr, int protocolServerPort,
                          String bootStrapServerAddr, int bootStrapServerPort,
                          String bootStrapClientAddr, int bootStrapClientPort)
                                                             throws Exception{
    
    this.protocolServer = new ProtocolServer(protocolServerAddr,
                                             protocolServerPort);

    this.bootstrapClient = new BootStrapClient(bootStrapServerAddr,
                                               bootStrapServerPort,
                                               bootStrapClientAddr,
                                               bootStrapClientPort);
  }

  public void start() throws Exception{

    while(true){
      for(String peerIP : bootstrapClient.getPeersList()){
        System.out.println(peerIP);
      }
      System.out.println();
      //Pause for 2 seconds
      Thread.sleep(2000);
    }
  }
}

