package com.project.gossip.p2p;

import java.util.Iterator;
import java.util.Set;

import java.lang.Exception;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

public class ProtocolDriver{

  private ProtocolServer server;

  public ProtocolDriver(String addr, int port) throws Exception{
    this.server = new ProtocolServer(addr, port);
  }

  public static void main(String [] args) throws Exception{

    ProtocolCli cli = new ProtocolCli(args);
    cli.parse();
    System.out.println(cli.configFilePath);
    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(
                                                cli.configFilePath);

    SubnodeConfiguration gossipSec = confFile.getSection(cli.gossipSectionName);
    Iterator it1 = gossipSec.getKeys();

            while (it1.hasNext()) {
                // Get element 
                Object key = it1.next(); 
                System.out.print("Key " + key.toString() +  " Value " +
                  gossipSec.getString(key.toString()) + "\n");
            }
  }
}

