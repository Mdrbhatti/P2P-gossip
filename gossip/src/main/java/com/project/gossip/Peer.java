package com.project.gossip;

import com.project.gossip.api.ApiServer;
import com.project.gossip.bootstrap.BootStrapServer;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.p2p.MaintainOverlay;
import com.project.gossip.p2p.ProtocolServer;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

public class Peer {

  private ProtocolServer protocolServer;
  private ApiServer apiServer;
  private BootStrapServer bootStrapServer;

  // Number of peers the current peer has to exchange information with
  private int degree;

  private String protocolServerAddr;
  private int protocolServerPort;

  private String apiServerAddr;
  private int apiServerPort;

  private String bootStrapServerAddr;
  private int bootStrapServerPort;

  private int cacheSize;

  private long send_peer_list_delay = 10000;

  private MaintainOverlay maintainOverlay;

  public Peer(SubnodeConfiguration conf, ProtocolCli cli) throws Exception {

    String[] bootStrapServerConf = serverConf(conf, "bootstrapper");
    this.bootStrapServerAddr = bootStrapServerConf[0];
    this.bootStrapServerPort = Integer.parseInt(bootStrapServerConf[1]);

    if (!cli.isBootStrapServer) {
      this.degree = Integer.parseInt(conf.getString("max_connections"));
      if (this.degree < 1) {
        P2PLogger.error("Degree can not be less than 1");
        P2PLogger.error("Exiting..");
        System.exit(-1);
      }

      String[] p2pServerConf = serverConf(conf, "listen_address");
      this.protocolServerAddr = p2pServerConf[0];
      this.protocolServerPort = Integer.parseInt(p2pServerConf[1]);

      String[] apiServerConf = serverConf(conf, "api_address");
      this.apiServerAddr = apiServerConf[0];
      this.apiServerPort = Integer.parseInt(apiServerConf[1]);

      this.cacheSize = Integer.parseInt(conf.getString("cache_size"));
      if (conf.containsKey("peer_list_send_delay")) {
        this.send_peer_list_delay = Long.parseLong(conf.getString
            ("peer_list_send_delay"));
      }

      PeerKnowledgeBase.maxCacheSize = cacheSize;

      protocolServer = new ProtocolServer(protocolServerAddr, protocolServerPort, bootStrapServerAddr,
          bootStrapServerPort, degree);

      apiServer = new ApiServer(apiServerAddr, apiServerPort);

      maintainOverlay = new MaintainOverlay(send_peer_list_delay, protocolServer);

      printConf();
    }
    else {
      this.bootStrapServer = new BootStrapServer(this.bootStrapServerPort,
          this.bootStrapServerAddr);
    }
  }

  public static void main(String[] args) throws Exception {

    ProtocolCli cli = new ProtocolCli(args);
    cli.parse();

    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(cli.configFilePath);
    SubnodeConfiguration conf = confFile.getSection(cli.gossipSectionName);

    // Initialize logger
    String level = conf.getString("log_level");
    P2PLogger logger = new P2PLogger(conf.getString("listen_address").split(":")[0] + ".log", level);

    Peer driver = new Peer(conf, cli);
    driver.start(cli.isBootStrapServer);
  }

  private String[] serverConf(SubnodeConfiguration conf, String key) {
    return conf.getString(key).split(":");
  }

  public void printConf() {
    P2PLogger.info("P2p Server Addr: " + protocolServerAddr);
    P2PLogger.info("P2p Server Port: " + protocolServerPort);
    P2PLogger.info("Api Server Addr: " + apiServerAddr);
    P2PLogger.info("Api Server Port: " + apiServerPort);
    P2PLogger.info("Bootstrap Server Addr: " + bootStrapServerAddr);
    P2PLogger.info("Bootstrap Server Port: " + bootStrapServerPort);
    P2PLogger.info("Degree: " + degree);
    P2PLogger.info("Send Peer List Delay: " + send_peer_list_delay);
  }

  public void start(boolean isBootStrapServer) {
    if (!isBootStrapServer) {
      this.protocolServer.start();
      this.maintainOverlay.start();
      this.apiServer.start();
    }
    else {
      this.bootStrapServer.listen();
    }
  }

  public String getProtocolServerAddr() {
    return protocolServerAddr;
  }
}
