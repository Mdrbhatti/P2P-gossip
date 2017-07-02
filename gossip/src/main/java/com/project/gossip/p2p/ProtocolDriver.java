package com.project.gossip.p2p;

import java.util.Iterator;
import java.util.Set;

import java.lang.Exception;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

public class ProtocolDriver{

  private ProtocolServer server;

  //Number of peers the current peer has to exchange information with
  private int degree;

  private String protocolServerAddr;
  private int protocolServerPort;

  private String bootStrapServerAddr;
  private int bootStrapServerPort;

  public ProtocolDriver(SubnodeConfiguration conf) throws Exception{
  
    this.degree = Integer.parseInt(conf.getString("max_connections"));

    String [] p2pServerConf = serverConf(conf, "listen_address");
    this.protocolServerAddr = p2pServerConf[0];
    this.protocolServerPort = Integer.parseInt(p2pServerConf[1]);

    String [] bootStrapServerConf = serverConf(conf, "bootstrapper");
    this.bootStrapServerAddr = bootStrapServerConf[0];
    this.bootStrapServerPort =  Integer.parseInt(bootStrapServerConf[1]);

  }

  private String [] serverConf(SubnodeConfiguration conf, String key){
    return conf.getString(key).split(":");
  }

  public void printConf(){
    System.out.println("Degree: "+degree);
    System.out.println("P2p Server Addr: "+protocolServerAddr);
    System.out.println("P2p Server Port: "+protocolServerPort);
    System.out.println("Bootstrap Server Addr: "+bootStrapServerAddr);
    System.out.println("Bootstrap Server Port: "+bootStrapServerPort);

  }

  public static void main(String [] args) throws Exception{

    ProtocolCli cli = new ProtocolCli(args);
    cli.parse();

    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(
                                                cli.configFilePath);

    SubnodeConfiguration gossipSec = confFile.getSection(cli.gossipSectionName);
    ProtocolDriver driver = new ProtocolDriver(gossipSec);

    //print set configurations
    driver.printConf();
  }
}

