package com.project.gossip.logger;

import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class P2PLogger {
	public static Logger logger = Logger.getLogger("P2P");
	public static FileHandler fh;

	public P2PLogger(String filePath, String level) {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"[%1$tH:%1$tM:%1$tS] %4$s: %5$s%6$s%n");
		try {
			logger.setLevel(Level.parse(level));
			fh = new FileHandler(filePath);
			logger.addHandler(fh);
			fh.setFormatter(new SimpleFormatter());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void log(Level level, String msg) {
		if (logger != null) {
			logger.log(level, msg);
		}
	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void error(String msg) {
		logger.severe(msg);
	}

}