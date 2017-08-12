package com.project.gossip;

import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.MessageType;
import com.project.gossip.message.messages.GossipAnnounce;
import com.project.gossip.message.messages.GossipNotification;
import com.project.gossip.CacheItem;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PeerKnowledgeBase {
  // key is ip address
  public static Map<String, SocketChannel> connectedPeers =
      Collections.synchronizedMap(new HashMap<String, SocketChannel>());

  //<datatype, Array of modules that sent notify msg for this datatype
  public static Map<Short, ArrayList<SocketChannel>> validDatatypes =
      Collections.synchronizedMap(new HashMap<Short, ArrayList<SocketChannel>>());

  //  public static Map<Short, ArrayList<GossipAnnounce>> waitingForValidation =
  //      Collections.synchronizedMap(new HashMap<Short, ArrayList<GossipAnnounce>>());

  public static List<CacheItem> dataItems = Collections.synchronizedList(new LinkedList<CacheItem>());

  public static short messageId = 0;

  public static ArrayList<String> getConnectedPeerIPs() {
    return new ArrayList<String>(connectedPeers.keySet());
  }

  public static Boolean containsDatatype(short datatype){
    return validDatatypes.containsKey(datatype);
  }

  public static void sendNotificationToModules(short datatype,
      GossipAnnounce gossipAnnounceMsg){
    if(messageId == Short.MAX_VALUE){
      messageId = 0;
    }
    GossipNotification gossipNotificationMsg = null;
    try{
      gossipNotificationMsg = new GossipNotification(
          gossipAnnounceMsg.getSize(), MessageType.GOSSIP_NOTIFICATION.getVal(),
          messageId, gossipAnnounceMsg.getDatatype(), gossipAnnounceMsg.getData());
      messageId++;

    }
    catch (Exception exp){
      System.out.println("Unable to convert gossip announce msg to gossip notification");
      exp.printStackTrace();
      return;
    }
    
    // Send notification to all registered modules
    // Wait for a single validation
    // Store message ID + gossip announce message
    ByteBuffer writeBuffer = null;
    try {
      writeBuffer = gossipNotificationMsg.getByteBuffer();
    } catch (Exception e) {
      System.out.println("Couldn't construct Gossip Notification Message");
      e.printStackTrace();
      return;
    }
    if(writeBuffer!=null){
      writeBuffer.flip();
      sendBufferToAllSubscribedModules(writeBuffer, "Gossip Notification", datatype );
      // Add cached item (used by protocol server after gossip validation)
      addDataItem(new CacheItem(gossipAnnounceMsg, messageId));
    }
  }

  public static void addDataItem(CacheItem item){
    dataItems.add(item);
  }

  public static void addValidDatatype(short datatype, SocketChannel channel) {
    if (!validDatatypes.containsKey(datatype)) {
      ArrayList<SocketChannel> arr = new ArrayList<SocketChannel>();
      arr.add(channel);
      validDatatypes.put(datatype, arr);
    } else {
      validDatatypes.get(datatype).add(channel);
    }
  }
  
  // TODO: TEST THIS?
  public static void sendBufferToAllSubscribedModules(ByteBuffer buffer, String msg, Short datatype) {
    ArrayList<SocketChannel> modules = validDatatypes.get(datatype);
    for (SocketChannel channel: modules) {
      P2PLogger.log(Level.INFO, "Sending " + msg + " to module");
      buffer.rewind();
      if (channel.isConnected()) {
        try {
          while (buffer.hasRemaining()) {
            channel.write(buffer);
          }
        } catch (IOException exp) {
          P2PLogger.log(Level.INFO, "Error while sending to " + module);
          exp.printStackTrace();
        }
      }
    }
  }

  public static void sendBufferToAllPeers(ByteBuffer buffer, String msg) {
    for (String peer : connectedPeers.keySet()) {
      P2PLogger.log(Level.INFO, "Sending " + msg + " to: " + peer);
      buffer.rewind();
      SocketChannel channel = connectedPeers.get(peer);
      if (channel.isConnected()) {
        try {
          while (buffer.hasRemaining()) {
            channel.write(buffer);
          }
        } catch (IOException exp) {
          P2PLogger.log(Level.INFO, "Error while sending to " + peer);
          exp.printStackTrace();
        }
      }
    }
  }
}
