package com.project.gossip.p2p.bootstrap;

import com.project.gossip.server.UdpServer;
import com.project.gossip.logger.P2PLogger;

import com.project.gossip.p2p.ProtocolCli;
import com.project.gossip.p2p.messageReader.HelloMessageReader;
import com.project.gossip.p2p.messages.HelloMessage;
import com.project.gossip.p2p.messages.PeerList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;
import java.util.HashSet;

import java.util.logging.Logger;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.util.ArrayList;

import java.lang.Integer;

public class BootStrapServer{
  private DatagramChannel serverSocket;
  private ByteBuffer readBuffer;
  private HashSet<String> peers = new HashSet<String>();
  private static Logger logger;

  public BootStrapServer(int port, String addr) throws Exception{
    this.serverSocket = new UdpServer(port, addr).getServerSocket();
    //TODO: remove hardcoding
    this.readBuffer = ByteBuffer.allocate(64*128);
  }
  
  public void setLogger(Logger logger){
    this.logger = logger;
  }

  public void listen() throws Exception{
    while(true){
      InetSocketAddress clientAddress = (InetSocketAddress) 
                                        this.serverSocket.receive(readBuffer);

      //could happen
      if(clientAddress == null){
        logger.info("Address was null");
        continue;
      }

      //validates a hello message
      HelloMessage msg = HelloMessageReader.read(readBuffer);
      readBuffer.clear();

      if( msg == null){
        logger.info("Invalid Hello Message Recv");
      }
      else{
        String address = clientAddress.getAddress().getHostAddress();
        peers.add(address);

        logger.info("Someone connected: "+ address);
        logger.info("Sending peers list");

        //reply with peers list
        PeerList peerListMsg = new PeerList(new ArrayList<String>(peers));

        ByteBuffer writeBuffer = peerListMsg.getByteBuffer();
        writeBuffer.flip();
        int bytesSent = serverSocket.send(writeBuffer, clientAddress);
        writeBuffer.clear();
      }     
    }
  }

  public static void main(String [] args) throws Exception{
    ProtocolCli cli = new ProtocolCli(args);
    cli.parse();
    String path = "/home/mdrbhatti/Desktop/logs/log.txt";
    String name = "Bootstrap";
    String level= "INFO";
    Logger logger = new P2PLogger().getNewLogger(name,path,level);
    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(
                                                cli.configFilePath);

    SubnodeConfiguration conf = confFile.getSection(cli.gossipSectionName);
    String [] bootStrapServerConf = conf.getString("bootstrapper").split(":");
    BootStrapServer server = new BootStrapServer(
        Integer.parseInt(bootStrapServerConf[1]),
        bootStrapServerConf[0]);
    server.setLogger(logger);
    server.listen();
    logger.info("Hamza");
    
    System.out.println("hamza");
  }

}
