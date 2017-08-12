package com.project.gossip;

import java.lang.Exception;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.project.gossip.p2p.GossipPeerListThread;
import com.project.gossip.p2p.ProtocolCli;
import com.project.gossip.p2p.ProtocolServer;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import com.project.gossip.api.ApiServer;
import com.project.gossip.logger.P2PLogger;

public class Peer {

  private ProtocolServer protocolServer;
  private ApiServer apiServer;

  // Number of peers the current peer has to exchange information with
  private int degree;

  private String protocolServerAddr;
  private int protocolServerPort;

  private String apiServerAddr;
  private int apiServerPort;

  private String bootStrapServerAddr;
  private int bootStrapServerPort;

  private GossipPeerListThread gossipPeerListThread;
  private static Logger logger;

  public Peer(SubnodeConfiguration conf, ProtocolCli cli) throws Exception {

    this.degree = Integer.parseInt(conf.getString("max_connections"));

    String[] p2pServerConf = serverConf(conf, "listen_address");
    this.protocolServerAddr = p2pServerConf[0];
    this.protocolServerPort = Integer.parseInt(p2pServerConf[1]);

    String[] bootStrapServerConf = serverConf(conf, "bootstrapper");
    this.bootStrapServerAddr = bootStrapServerConf[0];
    this.bootStrapServerPort = Integer.parseInt(bootStrapServerConf[1]);

    String[] apiServerConf = serverConf(conf, "api_address");
    this.apiServerAddr = apiServerConf[0];
    this.apiServerPort = Integer.parseInt(apiServerConf[1]);

    // print set configurations
    printConf();

    protocolServer = new ProtocolServer(protocolServerAddr, protocolServerPort, bootStrapServerAddr,
        bootStrapServerPort);

    apiServer = new ApiServer(apiServerAddr, apiServerPort);

    gossipPeerListThread = new GossipPeerListThread();
  }

  private String[] serverConf(SubnodeConfiguration conf, String key) {
    return conf.getString(key).split(":");
  }


  public void printConf() {
    P2PLogger.log(Level.INFO, "Degree: " + degree);
    P2PLogger.log(Level.INFO, "P2p Server Addr: " + protocolServerAddr);
    P2PLogger.log(Level.INFO, "P2p Server Port: " + protocolServerPort);
    P2PLogger.log(Level.INFO, "Api Server Addr: " + apiServerAddr);
    P2PLogger.log(Level.INFO, "Api Server Port: " + apiServerPort);
    P2PLogger.log(Level.INFO, "Bootstrap Server Addr: " + bootStrapServerAddr);
    P2PLogger.log(Level.INFO, "Bootstrap Server Port: " + bootStrapServerPort);
  }

  public void start() {
    this.protocolServer.start();
    this.gossipPeerListThread.start();
    //this.apiServer.start();
  }

  public static void main(String[] args) throws Exception {

    ProtocolCli cli = new ProtocolCli(args);
    cli.parse();

    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(cli.configFilePath);
    SubnodeConfiguration conf = confFile.getSection(cli.gossipSectionName);

    // Initialize logger
    String id = conf.getString("id");
    P2PLogger logger = new P2PLogger("peer", "peer" + id + ".log", "INFO");

    Peer driver = new Peer(conf, cli);
    driver.start();
  }
}