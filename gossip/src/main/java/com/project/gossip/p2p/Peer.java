package com.project.gossip.p2p;

import java.lang.Exception;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

public class Peer {

  private ProtocolServer server;

  //Number of peers the current peer has to exchange information with
  private int degree;

  private static String protocolServerAddr;
  private static int protocolServerPort;

  private String bootStrapServerAddr;
  private int bootStrapServerPort;

  private String bootStrapClientAddr;
  private int bootStrapClientPort;

  private ProtocolServer protocolServer;
  private GossipPeerListThread gossipPeerListThread;

  public Peer(SubnodeConfiguration conf, ProtocolCli cli)
                                                            throws Exception{
  
    this.degree = Integer.parseInt(conf.getString("max_connections"));

    String [] p2pServerConf = serverConf(conf, "listen_address");
    this.protocolServerAddr = p2pServerConf[0];
    this.protocolServerPort = Integer.parseInt(p2pServerConf[1]);

    String [] bootStrapServerConf = serverConf(conf, "bootstrapper");
    this.bootStrapServerAddr = bootStrapServerConf[0];
    this.bootStrapServerPort =  Integer.parseInt(bootStrapServerConf[1]);

    this.bootStrapClientAddr = cli.peerLocalAddr;
    this.bootStrapClientPort =  cli.peerLocalPort;

    //print set configurations
    printConf();

    protocolServer = new ProtocolServer(protocolServerAddr,
            protocolServerPort, bootStrapServerAddr, bootStrapServerPort);

    gossipPeerListThread = new GossipPeerListThread(protocolServer);
  }

  private String [] serverConf(SubnodeConfiguration conf, String key){
    return conf.getString(key).split(":");
  }

  public static int getProtocolServerPort(){
    return protocolServerPort;
  }

  public static String getProtocolServerAddr(){
    return protocolServerAddr;
  }

  public void printConf(){
    System.out.println("Degree: "+degree);
    System.out.println("P2p Server Addr: "+protocolServerAddr);
    System.out.println("P2p Server Port: "+protocolServerPort);
    System.out.println("Bootstrap Server Addr: "+bootStrapServerAddr);
    System.out.println("Bootstrap Server Port: "+bootStrapServerPort);
  }

  public void start(){
    this.protocolServer.start();
    this.gossipPeerListThread.start();
  }

  public static void main(String [] args) throws Exception{

    ProtocolCli cli = new ProtocolCli(args);
    cli.parse();

    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(
                                                cli.configFilePath);

    SubnodeConfiguration gossipSec = confFile.getSection(cli.gossipSectionName);
    Peer driver = new Peer(gossipSec, cli);
    driver.start();

  }
}

