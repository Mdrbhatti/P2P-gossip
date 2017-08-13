package com.project.gossip.p2p.bootstrap;

import com.project.gossip.server.UdpServer;
import com.project.gossip.logger.P2PLogger;

import com.project.gossip.ProtocolCli;
import com.project.gossip.message.messageReader.HelloMessageReader;
import com.project.gossip.message.messages.HelloMessage;
import com.project.gossip.message.messages.PeerList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.logging.Level;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.util.ArrayList;

import java.lang.Integer;

public class BootStrapServer {
  private DatagramChannel serverSocket;
  private ByteBuffer readBuffer;
  private HashSet<String> peers = new HashSet<String>();

  public BootStrapServer(int port, String addr) throws Exception {
    this.serverSocket = new UdpServer(port, addr).getServerSocket();
    //TODO: remove hardcoding
    this.readBuffer = ByteBuffer.allocate(64 * 128);
  }

  public void listen() {
    while (true) {
      InetSocketAddress clientAddress = null;
      try{
        clientAddress = (InetSocketAddress)
            this.serverSocket.receive(readBuffer);
      }
      catch (IOException exp){
        exp.printStackTrace();
      }

      //could happen
      if (clientAddress == null) {
        P2PLogger.log(Level.INFO, "Address was null");
        continue;
      }

      //validates a hello message
      readBuffer.flip();
      HelloMessage msg = HelloMessageReader.read(readBuffer);
      readBuffer.clear();

      if (msg == null) {
        P2PLogger.log(Level.INFO, "Invalid Hello Message Recv");
      } else {
        String address = clientAddress.getAddress().getHostAddress();
        peers.add(address);

        P2PLogger.log(Level.INFO, "Peer with " + address +" connected to bootstrap server");
        P2PLogger.log(Level.INFO, "Sending peers list to "+address);

        try{
          //reply with peers list
          PeerList peerListMsg = new PeerList(new ArrayList<String>(peers));

          ByteBuffer writeBuffer = peerListMsg.getByteBuffer();
          writeBuffer.flip();
          int bytesSent = serverSocket.send(writeBuffer, clientAddress);
          writeBuffer.clear();
        }
        catch (Exception exp){
          System.out.println("Unable to send peers list to "+ address);
          exp.printStackTrace();
        }
      }
    }
  }
}
