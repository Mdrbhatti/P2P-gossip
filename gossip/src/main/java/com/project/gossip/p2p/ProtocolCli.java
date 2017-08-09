package com.project.gossip.p2p;

import java.lang.Exception;

import org.apache.commons.cli.*;

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

    options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription("display help for cli")
            .create("h"));

    options.addOption(OptionBuilder.withLongOpt("config")
            .withDescription("path to configuration file")
            .hasArg().isRequired().create("c"));

    options.addOption(OptionBuilder.withLongOpt("section")
            .withDescription("gossip section name in conf file")
            .hasArg().create("s"));

    options.addOption(OptionBuilder.withLongOpt("localaddr")
            .withDescription("peer's local address")
            .hasArg().create("l"));

    options.addOption(OptionBuilder.withLongOpt("localport")
            .withDescription("peer's local port")
            .hasArg().create("p"));

  }

  public void parse() {

    CommandLineParser parser = new GnuParser();
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
