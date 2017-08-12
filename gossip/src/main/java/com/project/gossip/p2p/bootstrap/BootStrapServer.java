package com.project.gossip.p2p.bootstrap;

import com.project.gossip.server.UdpServer;
import com.project.gossip.logger.P2PLogger;

import com.project.gossip.p2p.ProtocolCli;
import com.project.gossip.message.messageReader.HelloMessageReader;
import com.project.gossip.message.messages.HelloMessage;
import com.project.gossip.message.messages.PeerList;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.logging.Level;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.util.ArrayList;

import java.lang.Integer;

public class BootStrapServer{
  private DatagramChannel serverSocket;
  private ByteBuffer readBuffer;
  private HashSet<String> peers = new HashSet<String>();

  public BootStrapServer(int port, String addr) throws Exception{
    this.serverSocket = new UdpServer(port, addr).getServerSocket();
    //TODO: remove hardcoding
    this.readBuffer = ByteBuffer.allocate(64*128);
  }

  public void listen() throws Exception{
    while(true){
      InetSocketAddress clientAddress = (InetSocketAddress)
                                        this.serverSocket.receive(readBuffer);

      //could happen
      if(clientAddress == null){
        P2PLogger.log(Level.INFO, "Address was null");
        continue;
      }

      //validates a hello message
      readBuffer.flip();
      HelloMessage msg = HelloMessageReader.read(readBuffer);
      readBuffer.clear();

      if( msg == null){
        P2PLogger.log(Level.INFO, "Invalid Hello Message Recv");
      }
      else{
        String address = clientAddress.getAddress().getHostAddress();
        peers.add(address);

        P2PLogger.log(Level.INFO, "Someone connected: " + address);
        P2PLogger.log(Level.INFO, "Sending peers list");

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
    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(
                                                cli.configFilePath);
    SubnodeConfiguration conf = confFile.getSection(cli.gossipSectionName);
    String [] bootStrapServerConf = conf.getString("bootstrapper").split(":");
    
    // Initialize logger
    P2PLogger logger = new P2PLogger("bootstrap", "bootstrap.log", "INFO");
    BootStrapServer server = new BootStrapServer(
        Integer.parseInt(bootStrapServerConf[1]),
        bootStrapServerConf[0]);
    server.listen();
  }
}
