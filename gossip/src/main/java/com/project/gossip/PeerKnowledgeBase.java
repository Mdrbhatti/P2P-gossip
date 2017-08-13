package com.project.gossip;

import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.MessageType;
import com.project.gossip.message.messages.GossipAnnounce;
import com.project.gossip.message.messages.GossipNotification;
import com.project.gossip.message.messages.GossipValidation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;

public class PeerKnowledgeBase {

  //< Peer IP, Array of connected peer IPs>
  public static Map<String, ArrayList<String>> knownPeers =
      Collections.synchronizedMap(new HashMap<String, ArrayList<String>>());

  // key is ip address
  public static Map<String, SocketChannel> connectedPeers =
      Collections.synchronizedMap(new HashMap<String, SocketChannel>());

  //<datatype, Array of modules that sent notify msg for this datatype
  public static Map<Short, ArrayList<SocketChannel>> validDatatypes =
      Collections.synchronizedMap(new HashMap<Short, ArrayList<SocketChannel>>());

  //cache holds all gossip announce messages waiting for validation from modules
  //TODO maintain max size of cache
  public static List<CacheItem> cache = Collections.synchronizedList(new LinkedList<CacheItem>());

  //Peer class parses conf file and sets max cache size
  public static int maxCacheSize;

  public static short messageId = 0;

  public static ArrayList<String> getConnectedPeerIPs() {
    return new ArrayList<String>(connectedPeers.keySet());
  }

  public static Boolean containsDatatype(short datatype) {
    return validDatatypes.containsKey(datatype);
  }

  public static void sendNotificationToModules(short datatype,
                                               GossipAnnounce gossipAnnounceMsg,
                                               SocketChannel originChannel) {
    if (messageId == Short.MAX_VALUE) {
      messageId = 0;
    }
    GossipNotification gossipNotificationMsg = null;
    try {
      gossipNotificationMsg = new GossipNotification(
          gossipAnnounceMsg.getSize(), MessageType.GOSSIP_NOTIFICATION.getVal(),
          messageId, gossipAnnounceMsg.getDatatype(), gossipAnnounceMsg.getData());
    } catch (Exception exp) {
      System.out.println("Unable to convert gossip announce msg to gossip notification");
      exp.printStackTrace();
      return;
    }

    // Send notification to all registered modules
    // Wait for a single validation
    // Store message ID + gossip announce message
    ByteBuffer writeBuffer = gossipNotificationMsg.getByteBuffer();

    if (writeBuffer != null) {
      writeBuffer.flip();
      sendBufferToAllSubscribedModules(writeBuffer, "Gossip Notification", datatype);
      addDataItem(new CacheItem(gossipAnnounceMsg, messageId, originChannel));
    }
    messageId++;
  }

  public static void sendGossipAnnounce(GossipValidation gossipValidation) {
    short messageId = gossipValidation.getMessageId();
    synchronized (cache) {
      Iterator it = cache.iterator();
      while (it.hasNext()) {

        CacheItem item = (CacheItem) it.next();

        if (item.getMessageId() == messageId) {
          GossipAnnounce gossipAnnounceMsg = item.getGossipAnnounceMsg();
          byte ttl = gossipAnnounceMsg.getTtl();
          ByteBuffer writeBuffer = null;

          if (ttl == 0) {
            //send
            writeBuffer = gossipAnnounceMsg.getByteBuffer();
            if (writeBuffer != null) {
              writeBuffer.flip();
              sendBufferToAllPeers(writeBuffer, "Gossip Announce Msg", item.getOriginPeer());
            }
          } else if (ttl > 1) {
            gossipAnnounceMsg.decrementTTL();

            writeBuffer = gossipAnnounceMsg.getByteBuffer();
            if (writeBuffer != null) {
              writeBuffer.flip();
              sendBufferToAllPeers(writeBuffer, "Gossip Announce Msg", item.getOriginPeer());
            }
          } else {
            //discard
            System.out.println("Last Hop, dont announce msg");
          }
          //remove cache item
          it.remove();
          return;
        }
      }
    }
  }

  public static void removeCacheItem(short messageId) {
    cache.remove(messageId);
  }

  // TODO add check if cache size exceeds limit
  public static void addDataItem(CacheItem item) {
    //remove oldest cacheitem to make space for latest items
    if (cache.size() == maxCacheSize) {
      cache.remove(0);
    }
    //adds items at the tail of linked list
    cache.add(item);
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
    for (SocketChannel channel : modules) {
      P2PLogger.log(Level.INFO, "Sending " + msg + " to module");
      buffer.rewind();
      if (channel.isConnected()) {
        try {
          while (buffer.hasRemaining()) {
            channel.write(buffer);
          }
        } catch (IOException exp) {
          P2PLogger.log(Level.INFO, "Error while sending " + msg + " to module");
          exp.printStackTrace();
        }
      }
    }
  }

  public static void sendBufferToAllPeers(ByteBuffer buffer, String msg,
                                          SocketChannel originChannel) {
    for (String peer : connectedPeers.keySet()) {
      P2PLogger.log(Level.INFO, "Sending " + msg + " to: " + peer);
      buffer.rewind();
      SocketChannel channel = connectedPeers.get(peer);
      if (channel.isConnected() && !channel.equals(originChannel)) {
        try {
          while (buffer.hasRemaining()) {
            channel.write(buffer);
          }
        } catch (IOException exp) {
          P2PLogger.log(Level.INFO, "Error while sending " + msg + " to " + peer);
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
          P2PLogger.log(Level.INFO, "Error while sending " + msg + " to " + peer);
          exp.printStackTrace();
        }
      }
    }
  }
}
