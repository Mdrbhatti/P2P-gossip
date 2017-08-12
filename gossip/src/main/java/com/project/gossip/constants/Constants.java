package com.project.gossip.constants;

public final class Constants {

  //64KB Max Message Size
  public static final int MAX_MESSAGE_LENGTH = 64 * 1024;

  //Minimum Message size in gossip protocol is 4 bytes
  //Hello message is the shortest message which does not contain payload
  public static final int MIN_MESSAGE_LENGTH = 4;

  //size field
  public static final short SIZE_LENGTH = 2;

  //type field
  public static final short TYPE_LENGTH = 2;

  //Header size
  public static final short HEADER_LENGTH = SIZE_LENGTH + TYPE_LENGTH;
}
