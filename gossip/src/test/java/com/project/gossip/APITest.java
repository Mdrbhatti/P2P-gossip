package com.project.gossip;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import com.project.gossip.api.messages.GossipAnnounce;
import com.project.gossip.api.messages.GossipNotify;
import com.project.gossip.api.messages.GossipValidation;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.MessageType;
import com.project.gossip.p2p.bootstrap.BootStrapClient;
import com.project.gossip.server.TcpServer;

public class APITest {
	private SocketChannel socketChannel;
	public Selector acceptAndReadSelector;

	//256KB buffer
	private final int BUFFER_SIZE = 256 * 1024;

	//single thread is servicing all channels, so no danger of conccurent access
	//same buffer used for reading and writing
	private ByteBuffer buffer = ByteBuffer.allocateDirect (BUFFER_SIZE); 
	// Send different kinds of messages

	public APITest(String protocolServerAddr, int protocolServerPort)
			throws
			Exception{
		acceptAndReadSelector = Selector.open();
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(new InetSocketAddress(protocolServerAddr, protocolServerPort));
		socketChannel.configureBlocking(false);

		while (!socketChannel.finishConnect()) {}
//
		if(socketChannel != null && socketChannel.isConnected()){
			registerChannelWithSelectors(socketChannel);
		}		
	}

	public void run(){
		try{
//			acceptAndReadEventLoop();
//			sendGossipAnnounce();
			sendMessages();
		}
		catch (Exception exp){
			exp.printStackTrace();
		}
	}
	
	public void sendMessages(){
//		sendGossipAnnounce();
		sendGossipValidation();
	}
	
	public void sendGossipAnnounce(){
		P2PLogger.log(Level.INFO, "Started API TEST Server");
		byte[] data = "Meow meow".getBytes();
		short reserved = 0;
		byte ttl = 4;
		short size = (short) ((8) + data.length);
		short datatype = 12;
		try {
//			GossipAnnounce msg = new GossipAnnounce(size, MessageType.GOSSIP_ANNOUNCE.getVal(), ttl, reserved, datatype, data);
			ByteBuffer buffer1 = ByteBuffer.allocate(size);
			buffer1.putShort(size);
//			buffer1.putShort(MessageType.GOSSIP_ANNOUNCE.getVal());
			buffer1.putShort((short)500);
			buffer1.put(ttl);
			buffer1.putShort(reserved);
			buffer1.putShort(datatype);
			buffer1.put(data);
			buffer1.flip();
		    socketChannel.write(buffer1);
		    buffer1.clear();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void sendGossipNotify(){
		P2PLogger.log(Level.INFO, "Started API TEST Server");
		byte reserved = 0;
		short size = (short) ((8));
		short datatype = 12;
		try {
			GossipNotify msg = new GossipNotify(size, MessageType.GOSSIP_ANNOUNCE.getVal(), reserved, datatype);
			ByteBuffer buffer1 = ByteBuffer.allocate(size);
			buffer1.putShort(size);
			buffer1.putShort(MessageType.GOSSIP_NOTIFY.getVal());
			buffer1.putShort(reserved);
			buffer1.putShort(datatype);
			buffer1.flip();
		    socketChannel.write(buffer1);
		    buffer1.clear();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void sendGossipValidation(){
		P2PLogger.log(Level.INFO, "Sending gossip validation");
		byte reserved = (byte)0xF1;
		short size = (short) ((8));
		short messageId = 12;
		try {
//			GossipValidation msg = new GossipValidation(size, MessageType.GOSSIP_ANNOUNCE.getVal(), reserved, messageId);
			ByteBuffer buffer1 = ByteBuffer.allocate(size);
			buffer1.putShort(size);
			buffer1.putShort(MessageType.GOSSIP_VALIDATION.getVal());
			buffer1.putShort(messageId);
			buffer1.put(reserved);
			buffer1.put(reserved);
			buffer1.flip();
		    socketChannel.write(buffer1);
		    buffer1.clear();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/* This event loop handles read data events and new connections events
	     whenever we have a new connection we also register it with writeSelector
	     which is responsible for returning all connections which are ready
	     for data to be written to them */

	public void acceptAndReadEventLoop() throws Exception{
		P2PLogger.log(Level.INFO, "Started API TEST Server");
		while(true){
//			Thread.sleep(5000);
//			sendHelloMessage(socketChannel);
			//wait for events
			int numOfChannelsReady = 0;
			try{
				numOfChannelsReady = acceptAndReadSelector.select(5000);
				P2PLogger.log(Level.INFO, "Num of channels ready: "+ numOfChannelsReady);

			}
			catch(IOException e){
				e.printStackTrace();
			}

			if(numOfChannelsReady == 0){
				//someother thread invoked wakeup() method of selector
				continue;
			}
			// write to the channel here
			

			// Iterate over the set of selected keys
			Iterator it = acceptAndReadSelector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = (SelectionKey) it.next();
				//event fired when some channel sends data
				if(key.isReadable()){
					P2PLogger.log(Level.INFO, "READ EVENT");
					P2PLogger.log(Level.INFO, "HASH "+key.hashCode());
					SocketChannel socketChannel = (SocketChannel) key.channel();
					this.buffer.clear();

					int numOfBytesRead=0;
					try {
						while((numOfBytesRead = socketChannel.read(this.buffer)) > 0){
//							P2PLogger.log(Level.INFO, "Bytes recvd: "+numOfBytesRead);
							System.out.println("Bytes recvd: "+numOfBytesRead);
						}
					} catch (IOException e) {
						// conn closed by remote disgracefully
						key.cancel();
						socketChannel.close();
						System.out.println("Channel was closed EVENT");
					}

					//read returns -1 when remote closes conn gracefully
					if (numOfBytesRead == -1) {
						key.channel().close();
						key.cancel();
						System.out.println("SOCKET CLOSED BY REMOTE HOST");
					}
				}
				// Remove key from selected set; it's been handled
				it.remove();
			}
		}
	}


	/**
	 * Spew a greeting to the incoming client connection.
	 * @param channel The newly connected SocketChannel to say hello to.
	 */
	public void sendHelloMessage(SocketChannel channel)
			throws Exception
	{
		P2PLogger.log(Level.INFO, "Sending Hello Msg");
		buffer.clear( );
		buffer.put ("Hi there!\r\n".getBytes( ));
		buffer.flip( );
		channel.write (buffer);
	}

	private void registerChannelWithSelectors(SocketChannel channel) 
			throws ClosedChannelException{
		channel.register(acceptAndReadSelector, SelectionKey.OP_READ);
	}


	// Send and receive different types of gossip messages
	public static void main(String[] args) {
		try{
		APITest tt = new APITest("127.0.0.1", 6001);
		tt.run();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
