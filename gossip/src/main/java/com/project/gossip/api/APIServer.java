package com.project.gossip.api;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.project.gossip.APITest;
import com.project.gossip.api.messages.GossipAnnounce;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.MessageType;
import com.project.gossip.p2p.Peer;
import com.project.gossip.p2p.bootstrap.BootStrapClient;
import com.project.gossip.p2p.messages.HelloMessage;
import com.project.gossip.server.TcpServer;
import com.project.gossip.message.MessageType;

public class APIServer extends Thread{
	private ServerSocketChannel serverSocket;
	private HashMap<String, SocketChannel> connectedModules;
	public Selector acceptAndReadSelector;
	private final int BUFFER_SIZE = 256 * 1024;
	private ByteBuffer buffer = ByteBuffer.allocateDirect (BUFFER_SIZE); 

	public APIServer(String protocolServerAddr, int protocolServerPort) throws Exception {

		this.serverSocket = new TcpServer(protocolServerPort, protocolServerAddr).getServerSocket();
		this.connectedModules = new HashMap<String, SocketChannel>();
		acceptAndReadSelector = Selector.open();
		serverSocket.register(acceptAndReadSelector, SelectionKey.OP_ACCEPT );
	}

	public void run(){
		try{
			acceptAndReadEventLoop();
		}
		catch (Exception exp){
			exp.printStackTrace();
		}
	}

	public void acceptAndReadEventLoop() throws Exception{
		P2PLogger.log(Level.INFO, "Started API Server");
		while(true){
			int numOfChannelsReady = 0;
			try{
				numOfChannelsReady = acceptAndReadSelector.select(5000);
				P2PLogger.log(Level.FINE, "Size of Connected Peers: "+connectedModules.size());
			}
			catch(IOException e){
				e.printStackTrace();
			}

			if(numOfChannelsReady == 0){
				continue;
			}

			// Iterate over the set of selected keys
			Iterator it = acceptAndReadSelector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = (SelectionKey) it.next();

				//new incoming connection event
				if(key.isAcceptable()) {
					P2PLogger.log(Level.FINE, "Accept Event Triggered");
					SocketChannel channel = acceptNewConnection(key);
					InetSocketAddress remoteAddr = (InetSocketAddress) channel.socket().getRemoteSocketAddress();
					
					if (channel != null && channel.isConnected()) {
						String ip = remoteAddr.getAddress().getHostAddress();
						int port = remoteAddr.getPort();
						channel.register(acceptAndReadSelector, SelectionKey.OP_READ);
						connectedModules.put(ip + ":" + port, channel);
						P2PLogger.log(Level.FINE, "Successfully Connected " + ip + ":" + port);
					}
				}
				//event fired when some channel sends data
				if(key.isReadable()){
					P2PLogger.log(Level.INFO, "READ EVENT");
					SocketChannel socketChannel = (SocketChannel) key.channel();
					this.buffer.clear();
					int numOfBytesRead=0;
					P2PLogger.log(Level.INFO, "READ EVENTx1");
					try {
						while((numOfBytesRead = socketChannel.read(this.buffer)) > 0){
							P2PLogger.log(Level.INFO, "Bytes recvd: "+numOfBytesRead);

						}
						System.out.println("MADE IT HERE");
						buffer.flip();
						// Construct objects here
						short size = buffer.getShort();
						short type = buffer.getShort();
						short reserved;
						short datatype;
						switch(type){
						case 500:
							// Decrease TTL
							P2PLogger.log(Level.INFO, "Received GOSSIP_ANNOUNCE: "+numOfBytesRead);
							byte ttl = buffer.get();
							reserved = buffer.getShort();
							datatype = buffer.getShort();
							byte[] dst = new byte[buffer.remaining()];
							buffer.get(dst);
							P2PLogger.log(Level.INFO, "Type: " + type + "\nSize: " + size+
									"TTL " + ttl + "reserved " + reserved + "datatype: " + datatype+
									"data" + new String(dst, "UTF-8"));
							break;
						case 501:
							P2PLogger.log(Level.INFO, "Received GOSSIP_NOTIFY: "+numOfBytesRead);
							reserved = buffer.getShort();
							datatype = buffer.getShort();
							P2PLogger.log(Level.INFO, "Type: " + type + "\nSize: " + size+ 
									"reserved: " + reserved + "datatype: " + datatype);
							break;
						case 503:
							byte res[] = new byte[2];
							P2PLogger.log(Level.INFO, "Received GOSSIP_VALIDATION: "+numOfBytesRead);
							short messageId = buffer.getShort();
							res[0] = buffer.get();
							res[1] = buffer.get();
							for (int i = 0; i < 16; i++) {
								int j=0;
								if(i>7){
									j=1;
								}
								System.out.print(BigInteger.valueOf(res[j]).testBit(i%8) ? "1": "0");
							}
							// some chutyap with network byte order
							P2PLogger.log(Level.INFO, "Type: " + type + "\nSize: " + size+
									 "messageid: " + messageId + "reserved: " +BigInteger.valueOf(res[1]).testBit(7));
							break;
						default:
							P2PLogger.log(Level.SEVERE, "Invalid message type. DROPPING!");
							break;
						}

						buffer.clear();

					} catch (IOException e) {
						// conn closed by remote disgracefully
						key.cancel();
						socketChannel.close();
						connectedModules.remove(socketChannel.socket().getInetAddress()
								.getHostAddress());
					}

					//read returns -1 when remote closes conn gracefully
					if (numOfBytesRead == -1) {
						key.channel().close();
						key.cancel();
						P2PLogger.log(Level.SEVERE, "SOCKET CLOSED BY REMOTE HOST");
						connectedModules.remove(socketChannel.socket().getInetAddress()
								.getHostAddress());
					}
				}
				it.remove();
			}
		}
	}
	
	private SocketChannel acceptNewConnection(SelectionKey key) throws IOException{
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverChannel.accept();
		if(socketChannel == null){
			//could happen because serverChannel is non-blocking
			return null;
		}

		socketChannel.configureBlocking(false);
		return socketChannel;
	}

	public HashMap<String,SocketChannel> getConnectedModules(){
		return connectedModules;
	}

	public static void main(String[] args) {
		try{
			APIServer tt = new APIServer("127.0.0.1", 6001);
			tt.run();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
