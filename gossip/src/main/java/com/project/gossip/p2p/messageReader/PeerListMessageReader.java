package com.project.gossip.p2p.messageReader;

import com.project.gossip.p2p.messages.PeerList;

import java.lang.Exception;
import java.util.ArrayList;
import java.net.InetAddress;
import java.nio.ByteBuffer;


public class PeerListMessageReader{

  public PeerListMessageReader(){

  }

  public static PeerList read(ByteBuffer buffer){
    PeerList msg = null;

    try{
      //check for buffer underflow
      if(!buffer.hasRemaining()){
        return null;
      }

      buffer.flip();

      short size = buffer.getShort();
      short type = buffer.getShort();
      short numOfPeers = buffer.getShort();
      ArrayList<String> peerAddrList = new ArrayList<String>();

      for(int i=0;i<numOfPeers;i++){
        peerAddrList.add(convertIntIpToString(buffer.getInt()));
      }

      msg = new PeerList(size, type, numOfPeers, peerAddrList);

      buffer.clear();
    }
    catch(Exception exp){
      exp.printStackTrace();
      return null;
    }
    return msg;
  }
 
  private static String convertIntIpToString(int ip) throws Exception{
    return InetAddress.getByName(Integer.toString(ip)).getHostAddress();
  }
}

