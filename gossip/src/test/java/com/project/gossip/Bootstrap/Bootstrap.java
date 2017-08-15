package com.project.gossip.Bootstrap;

import com.project.gossip.bootstrap.BootStrapClient;
import com.project.gossip.bootstrap.BootStrapServer;
import org.junit.*;

public class Bootstrap {

	private static BootStrapServer bootStrapServer;
	private static BootStrapClient bootStrapClient;

	private static String bootStrapServerAddr = "127.0.0.1";
	private static int bootStrapServerPort = 7001;


	//execute only once, in the starting
	@BeforeClass
	public static void beforeClass() throws Exception{
		bootStrapServer = new BootStrapServer(bootStrapServerPort,bootStrapServerAddr);
		bootStrapServer.start();
	}

	public static BootStrapClient getNewClient(String bootStrapClientIp) throws
			Exception{
		return new BootStrapClient(bootStrapServerAddr, bootStrapServerPort,
				bootStrapClientIp);
	}

	@Test
	public void getPeerListFromBootStrapServer() throws Exception{
		/*when bootstrap client connects to bootstrap server, bootstrap server
		  will come to know about a new peer and it returns the IP of that peer
		  peer list size will be one because there is only one peer */
		Assert.assertEquals(getNewClient("127.0.0.1").getPeersList().size(),1);

		/*create a new client, now bootstrapserver will know about 2 peers
		  127.0.0.1 and 127.0.0.2 */
		Assert.assertEquals(getNewClient("127.0.0.2").getPeersList().size(), 2);
	}

}
