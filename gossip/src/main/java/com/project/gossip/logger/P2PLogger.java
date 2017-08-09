package com.project.gossip.logger;
import java.util.logging.Level;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class P2PLogger {
  public static Logger logger;
  FileHandler fh;
  
  public Logger getNewLogger(String name, String filePath, String level){
    logger = Logger.getLogger(name);   
    try {
        logger.setLevel(Level.parse(level));
        fh = new FileHandler(filePath);
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  
    } catch (Exception e) {  
        e.printStackTrace();
        System.out.println("Failed to initialize logger!");
    }
    logger.info("Logging [" + level + "] for " + name + " enabled!");
    return logger;
  }
}