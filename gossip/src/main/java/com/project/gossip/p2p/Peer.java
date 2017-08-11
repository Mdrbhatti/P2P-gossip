package com.project.gossip.p2p;

import java.lang.Exception;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import com.project.gossip.api.APIServer;
import com.project.gossip.logger.P2PLogger;

public class Peer {

  private ProtocolServer server;

  // Number of peers the current peer has to exchange information with
  private int degree;

  private static String protocolServerAddr;
  private static int protocolServerPort;

  private String bootStrapServerAddr;
  private int bootStrapServerPort;

  private String bootStrapClientAddr;
  private int bootStrapClientPort;

  private ProtocolServer protocolServer;
  private GossipPeerList gossipPeerListThread;
  private static Logger logger;

  public Peer(SubnodeConfiguration conf, ProtocolCli cli) throws Exception {

    this.degree = Integer.parseInt(conf.getString("max_connections"));

    String[] p2pServerConf = serverConf(conf, "listen_address");
    this.protocolServerAddr = p2pServerConf[0];
    this.protocolServerPort = Integer.parseInt(p2pServerConf[1]);

    String[] bootStrapServerConf = serverConf(conf, "bootstrapper");
    this.bootStrapServerAddr = bootStrapServerConf[0];
    this.bootStrapServerPort = Integer.parseInt(bootStrapServerConf[1]);

    this.bootStrapClientAddr = cli.peerLocalAddr;
    this.bootStrapClientPort = cli.peerLocalPort;

    // print set configurations
    printConf();

    protocolServer = new ProtocolServer(protocolServerAddr, protocolServerPort, bootStrapServerAddr,
        bootStrapServerPort);

    gossipPeerListThread = new GossipPeerList(protocolServer);
  }

  private String[] serverConf(SubnodeConfiguration conf, String key) {
    return conf.getString(key).split(":");
  }

  public static int getProtocolServerPort() {
    return protocolServerPort;
  }

  public static String getProtocolServerAddr() {
    return protocolServerAddr;
  }

  public void printConf() {
    P2PLogger.log(Level.INFO, "Degree: " + degree);
    P2PLogger.log(Level.INFO, "P2p Server Addr: " + protocolServerAddr);
    P2PLogger.log(Level.INFO, "P2p Server Port: " + protocolServerPort);
    P2PLogger.log(Level.INFO, "Bootstrap Server Addr: " + bootStrapServerAddr);
    P2PLogger.log(Level.INFO, "Bootstrap Server Port: " + bootStrapServerPort);
  }

  public void start() {
    this.protocolServer.start();
    this.gossipPeerListThread.start();
  }

  public static void main(String[] args) throws Exception {

    ProtocolCli cli = new ProtocolCli(args);
    cli.parse();

    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(cli.configFilePath);
    SubnodeConfiguration conf = confFile.getSection(cli.gossipSectionName);

    // Initialize logger
    String id = conf.getString("id");
    P2PLogger logger = new P2PLogger("peer", "peer" + id + ".log", "INFO");
    
    String [] apiServerConf = conf.getString("api_address").split(":");
    
    APIServer server = new APIServer(apiServerConf[0], 
    		Integer.parseInt(apiServerConf[1]));
    
    Peer driver = new Peer(conf, cli);
    server.start();
    driver.start();
  }
}
