package com.project.gossip;

import com.project.gossip.logger.P2PLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class PeerKnowledgeBase {
    // key is ip address
    public static HashMap<String, SocketChannel> connectedPeers = new
            HashMap<String, SocketChannel>();

    public static ArrayList<String> getConnectedPeerIPs(){
        return new ArrayList<String>(connectedPeers.keySet());
    }

    //<datatype, Array of modules that sent notify msg for this datatype
    public static HashMap<Short, ArrayList<SocketChannel>> validDatatypes = new
    HashMap<Short, ArrayList<SocketChannel>>();

    public static void addValidDatatype(short datatype, SocketChannel channel){
        if(!validDatatypes.containsKey(datatype)){
            ArrayList<SocketChannel> arr = new ArrayList<SocketChannel>();
            arr.add(channel);
            validDatatypes.put(datatype,arr);
        }
        else{
            validDatatypes.get(datatype).add(channel);
        }
    }

    public static void sendBufferToAllPeers(ByteBuffer buffer, String msg){
        for(String peer: connectedPeers.keySet()){
            P2PLogger.log(Level.INFO, "Sending "+msg+" to: "+peer);
            buffer.rewind();
            SocketChannel channel = connectedPeers.get(peer);
            if(channel.isConnected()) {
                try{
                    while (buffer.hasRemaining()) {
                        channel.write(buffer);
                    }
                }
                catch (IOException exp){
                    P2PLogger.log(Level.INFO, "Error while sending to "+peer);
                    exp.printStackTrace();
                }
            }
        }
    }


}
