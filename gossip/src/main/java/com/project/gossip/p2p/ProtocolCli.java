package com.project.gossip.p2p;

import java.lang.Exception;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ProtocolCli {

  private String[] args;
  private Options options;

  public static String configFilePath;
  public static String gossipSectionName = "gossip";
  public static String peerLocalAddr = "127.0.0.1";
  public static int peerLocalPort = 9999;

  public ProtocolCli(String[] args) {
    
    this.args = args;
    this.options = new Options();

    options.addOption("h", "help", false, "display help for cli");
    options.addOption("c", "config", true, "path to configuration file");
    options.addOption("s", "section", true, "gossip section name in conf file");
    options.addOption("l", "localaddr", true, "peer's local address for "+
                      "binding bootstrap client default: 127.0.0.1");
    options.addOption("p", "localport", true, "peer's port for "+
                      "binding bootstrap client default: 9999");
  }

  public void parse() {

    CommandLineParser parser = new BasicParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);

      if (cmd.hasOption("h")){
        help();
      }
      
      if (cmd.hasOption("c")) {
        configFilePath = cmd.getOptionValue("c");
      }
      
      if(cmd.hasOption("s")){
        gossipSectionName = cmd.getOptionValue("s");
      }

      if(cmd.hasOption("l")){
        peerLocalAddr = cmd.getOptionValue("l");
      }

      if(cmd.hasOption("p")){
        peerLocalPort = Integer.parseInt(cmd.getOptionValue("p"));
      }
    } 
    catch (ParseException e) {
      System.out.println("Failed to parse args");
      help();
    }
  }

  private void help() {
    HelpFormatter formater = new HelpFormatter();
    formater.printHelp("Main", options);
    System.exit(0);
  }
}
