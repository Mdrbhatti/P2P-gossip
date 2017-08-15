package com.project.gossip.p2p;

import com.project.gossip.PeerKnowledgeBase;
import com.project.gossip.constants.Constants;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.MessageType;
import com.project.gossip.message.messageReader.GossipAnnounceReader;
import com.project.gossip.message.messages.GossipAnnounce;
import com.project.gossip.bootstrap.BootStrapClient;
import com.project.gossip.message.messageReader.HelloMessageReader;
import com.project.gossip.message.messageReader.PeerListMessageReader;
import com.project.gossip.message.messages.HelloMessage;
import com.project.gossip.message.messages.PeerList;
import com.project.gossip.server.TcpServer;

import java.net.InetSocketAddress;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;

//Protocol server maintains connections with Gossip Peers
public class ProtocolServer extends Thread {

	private ServerSocketChannel serverSocket;

	private String peerAddr;

	private int protocolServerPort;

	private int degree;

	private BootStrapClient bootStrapClient;

	//selector for new connections and read data events
	public Selector acceptAndReadSelector;

	//256KB buffer
	private final int BUFFER_SIZE = 256 * 1024;

	//single thread is servicing all channels, so no danger of conccurent access
	//same buffer used for reading and writing
	private ByteBuffer payloadBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

	//header buffer
	private ByteBuffer headerBuffer = ByteBuffer.allocate(Constants.HEADER_LENGTH);

	public ProtocolServer(String protocolServerAddr, int protocolServerPort,
												String bootStrapServerAddr, int bootStrapServerPort,
												int degree) throws Exception {

		this.serverSocket = new TcpServer(protocolServerPort, protocolServerAddr)
				.getServerSocket();

		this.protocolServerPort = protocolServerPort;
		this.peerAddr = protocolServerAddr;
		this.degree = degree;

		this.bootStrapClient = new BootStrapClient(bootStrapServerAddr,
				bootStrapServerPort, protocolServerAddr);

		//create read and accept selector for event loop
		acceptAndReadSelector = Selector.open();

		// Register server socket with the Selector for accept connection events
		serverSocket.register(acceptAndReadSelector, SelectionKey.OP_ACCEPT);
	}

	public void run() {
		P2PLogger.info("Protocol Server Started");
		acceptAndReadEventLoop();
	}

	/*
	* Handles events for new connections, read events and connection termination
  * events from Gossip Peers
  */
	public void acceptAndReadEventLoop() {

		List<String> peerList = null;
		try {
			peerList = bootStrapClient.getPeersList();
		} catch (Exception exp) {
			P2PLogger.error("BOOTSTRAPPING FAILED, Unable to get peers list from" +
					" bootstrap server");
			P2PLogger.error("Exiting...");
			exp.printStackTrace();
			System.exit(-1);
		}

		for (String peer : peerList) {
			if (!peer.equals(peerAddr) && !PeerKnowledgeBase.connectedPeers
					.containsKey(peer)) {
				initiateConnection(peer);
			}
		}

		while (true) {
			//wait for events
			int numOfChannelsReady = 0;
			try {
				numOfChannelsReady = acceptAndReadSelector.select(5000);
				P2PLogger.info("Size of Connected Peers: " + PeerKnowledgeBase
						.connectedPeers.size());
				P2PLogger.info("IPs of Connected Peers");
				for (String ip : PeerKnowledgeBase.connectedPeers.keySet()) {
					P2PLogger.info(ip);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (numOfChannelsReady == 0) {
				//someother thread invoked wakeup() method of selector
				continue;
			}

			// Iterate over the set of selected keys
			Iterator it = acceptAndReadSelector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();

				//new incoming connection event
				if (key.isAcceptable()) {
					P2PLogger.info("Some Peer sent a connection request to Protocol " +
							"Server");
					SocketChannel channel = acceptNewConnection(key);

					if (channel != null && channel.isConnected()) {
						if (PeerKnowledgeBase.connectedPeers.size() == degree) {
							//close conn
							denyConnection(channel);
						}
						else {
							registerChannelWithSelectors(channel);
							sendHelloMessage(channel, peerAddr);
						}
					}
				}
				//event fired when some channel sends data
				if (key.isReadable()) {
					SocketChannel socketChannel = (SocketChannel) key.channel();

					//clear the buffers
					headerBuffer.clear();
					payloadBuffer.clear();

					int bytesRead = 0;
					try {
						//read the header
						bytesRead = socketChannel.read(headerBuffer);

						//read returns -1 when remote closes conn gracefully
						if (bytesRead == -1) {
							key.cancel();
							closeConnection(socketChannel);
						}
						else {

							//change the header buffer to read mode
							headerBuffer.flip();

							short size = headerBuffer.getShort();
							short type = headerBuffer.getShort();

							//message reader will also read the header, move the position of
							//buffer to 0 for re-reading it
							headerBuffer.rewind();

							//read the payload
							while (bytesRead != size) {
								bytesRead += socketChannel.read(payloadBuffer);
							}

							//change the payload buffer to read mode
							payloadBuffer.flip();

							if (MessageType.GOSSIP_HELLO.getVal() == type) {

								if (PeerKnowledgeBase.connectedPeers.size() == degree) {
									denyConnection(socketChannel);
								}
								else {
									HelloMessage helloMessage = HelloMessageReader.read
											(headerBuffer, payloadBuffer);
									if (helloMessage != null) {
										P2PLogger.info("Hello Message Received from: " +
												helloMessage.getSourceIp());
										PeerKnowledgeBase.connectedPeers.put(
												helloMessage.getSourceIp(),
												socketChannel);
										P2PLogger.info("Successfully Connected to: "
												+ helloMessage.getSourceIp());
									}
								}
							}
							if (MessageType.GOSSIP_PEER_LIST.getVal() == type) {

								PeerList peerListMsg = PeerListMessageReader.read
										(headerBuffer, payloadBuffer);
								if (peerListMsg != null) {
									String neighborAddress = getPeerIpFromSocket(socketChannel);
									P2PLogger.info("Peers List Message Received from: " +
											neighborAddress);
									for (String ip : peerListMsg.getPeerAddrList()) {
										P2PLogger.info(ip);
									}
									PeerKnowledgeBase.knownPeers.put(neighborAddress,
											peerListMsg.getPeerAddrList());
								}
							}
							if (MessageType.GOSSIP_ANNOUNCE.getVal() == type) {
								GossipAnnounce gossipAnnounceMsg = GossipAnnounceReader.read
										(headerBuffer, payloadBuffer);
								if (gossipAnnounceMsg != null) {
									P2PLogger.info("Gossip Announce Msg Received from: " +
											getPeerIpFromSocket(socketChannel));
									short datatype = gossipAnnounceMsg.getDatatype();
									if (PeerKnowledgeBase.containsDatatype(datatype)) {
										PeerKnowledgeBase.sendNotificationToModules(datatype,
												gossipAnnounceMsg, socketChannel);
									}
									else {
										P2PLogger.info("No Module has subscribed for Datatype " +
												gossipAnnounceMsg.getDatatype() + ", Dropping Message");
									}
								}
							}
						}
					} catch (IOException e) {
						// conn closed by remote disgracefully
						key.cancel();
						closeConnection(socketChannel);
					}
				}
				// Remove key from selected set; it's been handled
				it.remove();
			}
		}
	}


	public void sendHelloMessage(SocketChannel channel, String sourceAddr) {
		try {
			HelloMessage helloMsg = new HelloMessage(sourceAddr);
			ByteBuffer writeBuffer = helloMsg.getByteBuffer();
			writeBuffer.flip();
			channel.write(writeBuffer);
			writeBuffer.clear();
		} catch (Exception exp) {
			P2PLogger.error("Unable to send Hello Message");
			exp.printStackTrace();
		}
	}

	private void registerChannelWithSelectors(SocketChannel channel) {

		try {
			channel.register(acceptAndReadSelector, SelectionKey.OP_READ);
		} catch (ClosedChannelException exp) {
			P2PLogger.error("Unable to register channel with selector");
			exp.printStackTrace();
		}
	}

	private SocketChannel acceptNewConnection(SelectionKey key) {

		//get the channel
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

		//accept conenction
		SocketChannel socketChannel = null;
		try {
			socketChannel = serverChannel.accept();
			if (socketChannel == null) {
				//could happen because serverChannel is non-blocking
				return null;
			}
			socketChannel.configureBlocking(false);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return socketChannel;
	}

	public void initiateConnection(String addr) {
		if (PeerKnowledgeBase.connectedPeers.size() < degree) {
			SocketChannel socketChannel = null;
			try {
				P2PLogger.info("Trying to connect to: " + addr);
				socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);
				socketChannel.connect(new InetSocketAddress(addr, protocolServerPort));
				while (!socketChannel.finishConnect()) {
				}
			} catch (IOException exp) {
				P2PLogger.error("Unable to connect to: " + addr);
				exp.printStackTrace();
			}

			if (socketChannel != null && socketChannel.isConnected()) {
				registerChannelWithSelectors(socketChannel);
				sendHelloMessage(socketChannel, peerAddr);
				try {
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}
		}
		else {
			P2PLogger.info("Conencted Peers equals to Degree, " +
					"discarding initiate connection request");
		}
	}

	public void closeConnection(SocketChannel channel) {
		try {
			String address = getPeerIpFromSocket(channel);
			//address is null when degree is violated and connection is closed
			//before adding any entry in connected peers list
			if (address == null) {
				P2PLogger.info("Connection Request denied by peer");
			}
			else {
				P2PLogger.info("Connection to peer " + address + " closed");
				PeerKnowledgeBase.connectedPeers.remove(address);
			}
			channel.close();
		} catch (IOException exp) {
			exp.printStackTrace();
		}
	}

	public void denyConnection(SocketChannel socketChannel) {
		try {
			P2PLogger.info("Incoming Connection Request Denied, " +
					"connected peers size " + PeerKnowledgeBase.connectedPeers.size() +
					" is equal to degree " + degree);
			socketChannel.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/*
	 * Hashmap of connectedPeers contains <ip,socket> entries
	 * when peers close their connection we can use the getAddress function of
	 * socket, but during testing and dev phase we are running multiple peers
	 * on same machine using different IPs from private address space, when
	 * getAddress function is called on socket, it always returns 127.0.0.1
	 * eventhough the IP was 127.0.0.2.
	 * */
	public String getPeerIpFromSocket(SocketChannel channel) {
		for (String key : PeerKnowledgeBase.connectedPeers.keySet()) {
			if (PeerKnowledgeBase.connectedPeers.get(key).equals(channel)) {
				return key;
			}
		}
		return null;
	}

	public int getDegree() {
		return degree;
	}

	public String myIp() {
		return peerAddr;
	}
}

