package com.project.gossip.Messages;

import com.project.gossip.message.MessageType;
import com.project.gossip.message.messages.GossipAnnounce;
import org.junit.*;

public class GossipAnnounceTest {
  public static short size, type, datatype;
  public static byte ttl, reserved;
  public static byte[] data;

  @BeforeClass
  public static void beforeClass() throws Exception {
    // Set valid values
    type = MessageType.GOSSIP_ANNOUNCE.getVal();
    datatype = 11;
    ttl = 1;
    data = "Hello World".getBytes();
    reserved = 0;
    size = getCorrectSize();
  }

  public static short getCorrectSize() {
    return (short) ((Short.BYTES * 3) + (Byte.BYTES * 2) + (data.length));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createGossipAnnounceMessageWithInvalidType() throws Exception {
    // Invalid data type
    GossipAnnounce msg = new GossipAnnounce(size, (short) 123, ttl, reserved, datatype, data);
  }


  @Test(expected = IllegalArgumentException.class)
  public void createGossipAnnounceMessageWithNegativeTtl() throws Exception {
    // Not allowed
    GossipAnnounce msg = new GossipAnnounce(size, type, (byte) -1, reserved, datatype, data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createGossipAnnounceMessageWithInvalidSize() throws Exception {
    // Size != size with data
    GossipAnnounce msg = new GossipAnnounce((short) (size - 1), type, ttl, reserved, datatype, data);
  }

  @Test
  public void createValidGossipAnnounceMessage() throws Exception {
    GossipAnnounce msg = new GossipAnnounce(size, type, ttl, reserved, datatype, data);
    Assert.assertNotNull(msg);
  }


  @Test(expected = IllegalArgumentException.class)
  public void gossipAnnounceSizeBelowMinimum() throws Exception {
    // Valid size is between 4 - 65536
    GossipAnnounce msg = new GossipAnnounce((short) 1, type, ttl, reserved, datatype, data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void gossipAnnounceSizeAboveMaximum() throws Exception {
    // Valid size is between 4 - 65536
    GossipAnnounce msg = new GossipAnnounce((short) 65538, type, ttl, reserved, datatype, data);
  }
}
