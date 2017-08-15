package com.project.gossip.Messages;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.project.gossip.message.MessageType;
import com.project.gossip.message.messages.GossipValidation;


public class GossipValidationTest {
  static short size, type, messageId, reserved;
  
  @BeforeClass
  public static void beforeClass() throws Exception{
    size = getCorrectSize();
    type = MessageType.GOSSIP_VALIDATION.getVal();
    messageId = 22;
  }
  
  public static short getCorrectSize(){
    return (short) Short.BYTES * 4;
  }

  @Test
  public void creatMessageWithUnsetValidationBit() throws Exception{
    GossipValidation msg = new GossipValidation(size, type, messageId, (short)0);
    Assert.assertFalse(msg.isValid());
  }

  @Test
  public void creatMessageWithSetValidationBit() throws Exception{
    GossipValidation msg = new GossipValidation(size, type, messageId, (short)1);
    Assert.assertTrue(msg.isValid());
  }
}
