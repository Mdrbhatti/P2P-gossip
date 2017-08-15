package com.project.gossip.Messages;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.project.gossip.message.MessageType;
import com.project.gossip.message.messages.GossipAnnounce;
import com.project.gossip.message.messages.GossipNotification;

public class GossipNotificationTest {
  public static short size, type, messageId, datatype;
  public static byte[] data;

  @BeforeClass
  public static void beforeClass() throws Exception{
    // Set valid values
    type = MessageType.GOSSIP_NOTIFICATION.getVal();
    datatype = 11;
    messageId = 33;
    data = "Hello World".getBytes();
    size = getCorrectSize();
  }

  public static short getCorrectSize(){
    return (short) ((Short.BYTES * 4) + (data.length));
  }

  @Test(expected=IllegalArgumentException.class)
  public void createGossipNotificationeMessageWithInvalidSize() throws Exception{
    // Size != size with data
    GossipNotification msg = new GossipNotification((short)(size - 1), type, messageId, datatype, data);
  }

  @Test
  public void createValidGossipNotificationMessage() throws Exception{
    // valid message
    GossipNotification msg = new GossipNotification(size, type, messageId, datatype, data);
    Assert.assertNotNull(msg);
  }

}
