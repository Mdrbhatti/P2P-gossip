package com.project.gossip.constants;

public final class Helpers{

  public Helpers(){

  }

  public static byte[] shortToBytes(short s) {
    return new byte[]{(byte)(s & 0x00FF),(byte)((s & 0xFF00)>>8)};
  }  
}
