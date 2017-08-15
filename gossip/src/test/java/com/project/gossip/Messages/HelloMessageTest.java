package com.project.gossip.Messages;

import org.junit.Assert;
import org.junit.Test;

import com.project.gossip.message.messages.HelloMessage;

public class HelloMessageTest {
  
  @Test(expected=IllegalArgumentException.class)
  public void createMessageWithInvalidIp() throws Exception{
    HelloMessage msg = new HelloMessage("123.3.3.3.3");
  }
  
  @Test
  public void createValidMessage() throws Exception{
    HelloMessage msg = new HelloMessage("123.3.3.3");
    Assert.assertNotNull(msg);
  }
}
