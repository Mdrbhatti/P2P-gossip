package com.project.gossip.bootstrap;

import com.project.gossip.message.messages.HelloMessage;
import com.project.gossip.message.messages.PeerList;
import com.project.gossip.message.messageReader.PeerListMessageReader;

import com.project.gossip.constants.Constants;
import com.project.gossip.logger.P2PLogger;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.net.InetSocketAddress;
import java.util.List;

public class BootStrapClient {

	private DatagramChannel channel;
	private InetSocketAddress bootStrapServerAddr;
	private ByteBuffer payloadBuffer;
	private ByteBuffer headerBuffer;
	private int clientPort;
	private String clientAddr;

	public BootStrapClient(String serverAddr, int serverPort, String clientAddr)
			throws
			Exception {

		this.clientAddr = clientAddr;
		this.clientPort = clientPort;

		this.channel = DatagramChannel.open();

		this.channel.socket().bind(new InetSocketAddress(clientAddr, 0));
		this.bootStrapServerAddr = new InetSocketAddress(serverAddr, serverPort);
		this.channel.connect(this.bootStrapServerAddr);
		this.headerBuffer = ByteBuffer.allocate(Constants.HEADER_LENGTH);
		this.payloadBuffer = ByteBuffer.allocate(Constants.MAX_MESSAGE_LENGTH);
	}

	public List<String> getPeersList() {

		try {
			//send hello packet
			HelloMessage helloMsg = new HelloMessage(clientAddr);

			ByteBuffer writeBuffer = helloMsg.getByteBuffer();
			writeBuffer.flip();
			channel.write(writeBuffer);
			writeBuffer.clear();

			ByteBuffer[] arr = {headerBuffer, payloadBuffer};
			//read bytes from channel into header and payload buffer
			channel.read(arr);

			headerBuffer.flip();
			payloadBuffer.flip();
			PeerList peerListMsg = PeerListMessageReader.read(headerBuffer, payloadBuffer);
			headerBuffer.clear();
			payloadBuffer.clear();

			if (peerListMsg == null) {
				P2PLogger.info("Invalid Peer List Message Recvd");
				return null;
			}

			List<String> peerList = peerListMsg.getPeerAddrList();

			P2PLogger.info("Peer List Received from BootStrap Server");
			P2PLogger.info("Peer List size: " + peerList.size());
			for (String peer : peerList) {
				P2PLogger.info(peer);
			}

			return peerListMsg.getPeerAddrList();
		} catch (Exception exp) {
			P2PLogger.error("Unable to get peer list from bootstrap server");
			exp.printStackTrace();
			return null;
		}
	}
}
