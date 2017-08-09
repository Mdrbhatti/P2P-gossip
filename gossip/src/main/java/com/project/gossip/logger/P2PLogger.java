package com.project.gossip.logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class P2PLogger {
  Logger logger;
  
  public Logger getLogger(String name, String filePath){
    logger = Logger.getLogger(name);  
    FileHandler fh;  
    try {
        fh = new FileHandler(filePath);
//        "/home/mdrbhatti/Desktop/logs/log.txt"
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  
    } catch (Exception e) {  
        System.out.println("Failed to initialize logger!");
    }
    return logger;
  }
  
  
  

}
