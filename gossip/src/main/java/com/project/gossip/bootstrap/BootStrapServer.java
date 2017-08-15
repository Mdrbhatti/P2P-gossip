package com.project.gossip.bootstrap;

import com.project.gossip.constants.Constants;
import com.project.gossip.server.UdpServer;
import com.project.gossip.logger.P2PLogger;

import com.project.gossip.message.messageReader.HelloMessageReader;
import com.project.gossip.message.messages.HelloMessage;
import com.project.gossip.message.messages.PeerList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;

import java.util.ArrayList;

public class BootStrapServer extends Thread{
	private DatagramChannel serverSocket;
	private ByteBuffer readBuffer;
	private HashSet<String> peers = new HashSet<String>();

	public BootStrapServer(int port, String addr) throws Exception {
		this.serverSocket = new UdpServer(port, addr).getServerSocket();
		this.readBuffer = ByteBuffer.allocate(Constants.MAX_MESSAGE_LENGTH);
	}

	public void run(){
		listen();
	}

	public void listen() {
		P2PLogger.info("BootStrap Server Started...");
		while (true) {
			InetSocketAddress clientAddress = null;
			try {
				clientAddress = (InetSocketAddress)
						this.serverSocket.receive(readBuffer);
			} catch (IOException exp) {
				exp.printStackTrace();
			}

			//could happen
			if (clientAddress == null) {
				P2PLogger.info("Address was null");
				continue;
			}

			//validates a hello message
			readBuffer.flip();
			HelloMessage msg = HelloMessageReader.read(readBuffer);
			readBuffer.clear();

			if (msg == null) {
				P2PLogger.error("Invalid Hello Message Received");
			}
			else {
				String address = clientAddress.getAddress().getHostAddress();
				peers.add(address);

				P2PLogger.info("Host " + address + " connected to bootstrap "+
						"server");
				P2PLogger.info("Sending peers list to " + address);

				try {
					//shuffle Peer IPs
					ArrayList<String> IpAddresses = new ArrayList<String>(peers);
					Collections.shuffle(IpAddresses);

					//reply with peers list
					PeerList peerListMsg = new PeerList(IpAddresses);

					ByteBuffer writeBuffer = peerListMsg.getByteBuffer();
					writeBuffer.flip();
					int bytesSent = serverSocket.send(writeBuffer, clientAddress);
					writeBuffer.clear();
				} catch (Exception exp) {
					P2PLogger.error("Unable to send peers list to " + address);
					exp.printStackTrace();
				}
			}
		}
	}
}
