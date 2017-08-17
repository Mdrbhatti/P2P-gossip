package com.project.gossip.constants;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public final class Helpers {

  public static int convertIpStringToInt(String IP) throws Exception {
    return ByteBuffer.wrap(InetAddress.getByName(IP).getAddress()).getInt();
  }

  public static String convertIntIpToString(int ip) throws Exception {
    String strIp =
        String.format("%d.%d.%d.%d.",
            (ip & 0xff),
            (ip >> 8 & 0xff),
            (ip >> 16 & 0xff),
            (ip >> 24 & 0xff));

    String[] arr = strIp.split("\\.");
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (i = arr.length - 1; i > 0; i--) {
      sb.append(arr[i]);
      sb.append(".");
    }
    sb.append(arr[i]);
    return sb.toString();
  }
}
