package com.project.gossip.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class UdpServer extends Server{

  private DatagramChannel serverSocket;

  public UdpServer(int port, String addr) throws Exception{
    super(port, InetAddress.getByName(addr));

    this.serverSocket = DatagramChannel.open();
    this.serverSocket.socket().bind(new InetSocketAddress(getAddr(), port));
  }

  public DatagramChannel getServerSocket(){
    return this.serverSocket;
  }
}

