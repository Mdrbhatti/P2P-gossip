package com.project.gossip.contants;

public final class Constants{
  
  //64KB Max Message Size
  public static final int MAX_MESSAGE_LENGTH = 64 * 1024;
  
  //Minimum Message size in gossip protocol is 8 bytes
  //Gossip notify and Gossip validation are shortest messsages
  public static final int MIN_MESSAGE_LENGTH = 8;

  //size field
  public static final int SIZE_LENGTH = 2;

  //type field
  public static final int TYPE_LENGTH = 2;

  //Header size
  public static final int HEADER_LENGTH = SIZE_LENGTH + TYPE_LENGTH;
}
