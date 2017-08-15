package com.project.gossip.server;

import java.net.InetAddress;

public class Server {

  protected int port;
  protected InetAddress addr;

  public Server(int port, InetAddress addr) {
    this.port = port;
    this.addr = addr;
  }

  public int getPort() {
    return this.port;
  }

  public InetAddress getAddr() {
    return this.addr;
  }
}

