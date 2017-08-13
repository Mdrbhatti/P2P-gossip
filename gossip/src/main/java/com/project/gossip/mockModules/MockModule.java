package com.project.gossip.mockModules;

import com.project.gossip.ProtocolCli;
import com.project.gossip.constants.Constants;
import com.project.gossip.logger.P2PLogger;
import com.project.gossip.message.MessageType;
import com.project.gossip.message.messageReader.GossipNotificationReader;
import com.project.gossip.message.messageReader.GossipValidationReader;
import com.project.gossip.message.messages.GossipAnnounce;
import com.project.gossip.message.messages.GossipNotification;
import com.project.gossip.message.messages.GossipNotify;
import com.project.gossip.message.messages.GossipValidation;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.logging.Level;

public class MockModule {


  public static SocketChannel socketChannel = null;
  public static Scanner sc = null;

  public static void sendGossipAnnounce() throws Exception {
    System.out.println("Enter  the datatype for Gossip Announce Message: ");
    short datatype = sc.nextShort();
    byte[] data = "Meow Meowww!!".getBytes();
    byte reserved = 0;
    byte ttl = 4;
    short size = (short) (Constants.HEADER_LENGTH + 1 + 1 + 2 + data.length);
    short type = MessageType.GOSSIP_ANNOUNCE.getVal();

    GossipAnnounce gossipAnnounce = new GossipAnnounce(size, type, ttl, reserved, datatype, data);
    ByteBuffer writeBuffer = gossipAnnounce.getByteBuffer();

    if (writeBuffer != null) {
      writeBuffer.flip();
      socketChannel.write(writeBuffer);
      System.out.println("Gossip Announce Message Sent!");
    }
  }

  public static void sendGossipNotify() throws Exception {
    System.out.println("Enter  the datatype for Gossip Notify Message: ");
    short datatype = sc.nextShort();
    short reserved = 0;
    short size = (short) (Constants.HEADER_LENGTH + 2 + 2);
    short type = MessageType.GOSSIP_NOTIFY.getVal();

    GossipNotify gossipNotify = new GossipNotify(size, type, reserved, datatype);
    ByteBuffer writeBuffer = gossipNotify.getByteBuffer();

    if (writeBuffer != null) {
      writeBuffer.flip();
      socketChannel.write(writeBuffer);
      System.out.println("Gossip Notify Message Sent!");
      System.out.println("Waiting for Gossip Notification Message of datatype " + datatype);
    }
  }

  public static GossipNotification receiveGossipNotification() throws Exception {
    ByteBuffer buffer = ByteBuffer.allocate(64 * 1024 * 1024);
    socketChannel.read(buffer);
    buffer.flip();
    GossipNotification gossipNotification = GossipNotificationReader.read(buffer);
    System.out.println("Gossip Notification Received for datatype " +
        gossipNotification.getDatatype() + " data length " + gossipNotification.getData().length);
    return gossipNotification;
  }

  public static void sendValidationMessage(short messageId) throws Exception{
    short reserved = 0;
    int choice = -1;
    while(!(choice == 0 || choice == 1)){
      System.out.println("Press 1 to send validation or " +
          "Press 0 to send invalidation for message id "+messageId+" : ");
      choice = sc.nextInt();
    }

    if(choice == 1){
      reserved = (short) (reserved | 0x1);
    }

    short size = (short) (Constants.HEADER_LENGTH + 2 + 2);
    short type = MessageType.GOSSIP_VALIDATION.getVal();

    GossipValidation gossipValidation = new GossipValidation(size, type, messageId, reserved);
    ByteBuffer writeBuffer = gossipValidation.getByteBuffer();
    if(writeBuffer!=null){
      writeBuffer.flip();
      socketChannel.write(writeBuffer);
      System.out.println("Gossip Validation Message Sent for message ID: "+messageId);
    }
  }

  public static void main(String[] args) throws Exception {
    System.out.println("Welcome to Mock Module");

    ProtocolCli cli = new ProtocolCli(args);
    cli.parse();

    HierarchicalINIConfiguration confFile = new HierarchicalINIConfiguration(cli.configFilePath);
    SubnodeConfiguration conf = confFile.getSection(cli.gossipSectionName);

    String[] apiServerConf = conf.getString("api_address").split(":");
    String apiServerAddr = apiServerConf[0];
    int apiServerPort = Integer.parseInt(apiServerConf[1]);

    System.out.println("Opening connection to API Server " + apiServerAddr + ":" + apiServerPort);

    try {
      socketChannel = SocketChannel.open();
      //setting socket channel to BLOCKING
      socketChannel.configureBlocking(true);
      socketChannel.connect(new InetSocketAddress(apiServerAddr, apiServerPort));
    } catch (IOException exp) {
      exp.printStackTrace();
      System.out.println("Unable to connect to API server ");
      System.out.println("Exiting...");
      System.exit(-1);
    }

    System.out.println("Successfully Connected to API Server");

    sc = new Scanner(System.in);

    int choice = -1;
    while (true) {
      System.out.println("-------------------------------------");
      System.out.println("Enter 0 to send Gossip Announce Msg: ");
      System.out.println("Enter 1 to send Gossip Notify Msg: ");
      System.out.println("-------------------------------------");
      choice = sc.nextInt();
      switch (choice) {
        case 0:
          try {
            MockModule.sendGossipAnnounce();
          } catch (Exception exp) {
            System.out.println("Unable to send Gossip Announce");
            exp.printStackTrace();
          }
          break;
        case 1:
          //send gossip notify and wait for notification
          try {
            MockModule.sendGossipNotify();
            GossipNotification gossipNotification = MockModule.receiveGossipNotification();
            MockModule.sendValidationMessage(gossipNotification.getMessageId());
          } catch (Exception exp) {
            System.out.println("Unable to send Gossip Notify");
            exp.printStackTrace();
          }
          break;
        default:
          System.out.println("Please enter choice 0 or 1");
          break;
      }
    }
  }
}
