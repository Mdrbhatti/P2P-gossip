package com.project.gossip;

import java.lang.Exception;

import org.apache.commons.cli.*;

public class ProtocolCli {

  private String[] args;
  private Options options;

  public static String configFilePath;
  public static String gossipSectionName = "gossip";
  public static boolean isBootStrapServer = false;

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

    options.addOption(OptionBuilder.withLongOpt("bootstrap")
        .withDescription("start as bootstrap server")
        .create("b"));
  }

  public void parse() {

    CommandLineParser parser = new GnuParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        help();
      }

      if (cmd.hasOption("c")) {
        configFilePath = cmd.getOptionValue("c");
      }

      if (cmd.hasOption("s")) {
        gossipSectionName = cmd.getOptionValue("s");
      }

      if(cmd.hasOption("b")){
        isBootStrapServer = true;
      }

    } catch (ParseException e) {
      help();
    }
  }

  private void help() {
    HelpFormatter formater = new HelpFormatter();
    formater.printHelp("Main", options);
    System.exit(-1);
  }
}
