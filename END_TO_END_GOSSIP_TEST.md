#          Gossip Announce, Validation, Notification, Notify testing

This test makes use of a mock module to test the correct behavior of the various Gossip messages.

##                               TEST 1
If a peer opens connection to another peer whose maximum connections(degree) is
already satisfied, the connection request should be dropped


We will create 3 peers for testing, 2 mock modules. In total 3 different conf files will be used


NOTE: Please follow the steps below in order to see the result!

### Compile and package
```
cd P2P-gossip/gossip
mvn clean package
```

### Start BootStrap Server
Note that switch -b specifies that it is a bootstrap server

Command:
```java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.1.conf -b```

### PEER 1 conf
```
[gossip]
cache_size = 50
max_connections = 50
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.1:6001
api_address = 127.0.0.1:7001
log_level = FINE
peer_list_send_delay = 10000
```

### PEER 2 conf
```
[gossip]
cache_size = 50
max_connections = 50
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.2:6001
api_address = 127.0.0.2:7001
log_level = FINE
peer_list_send_delay = 10000
```

### Peer 3 conf
```
[gossip]
cache_size = 50
max_connections = 50
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.3:6001
api_address = 127.0.0.3:7001
log_level = FINE
peer_list_send_delay = 10000
```

### Execution order

1. Start peer 1 (P1)
	```java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.1.conf ```
2. Start peer 2 (P2)
	```java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.2.conf ```
3. Start peer 3 (P3)
	```java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.3.conf ```
4. Start mock module for peer 1 (M1)
	```java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.mockModules.MockModule -c config/gossip_127.0.0.1.conf ```
5. Start mock module for peer 2 (M2)
	```java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.mockModules.MockModule -c config/gossip_127.0.0.2.conf ```
6. Start mock module for peer 3 (M3)
	```java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.mockModules.MockModule -c config/gossip_127.0.0.3.conf ```

Note: Mock modules will open connections to the API servers of the peer (as given in the config file)

### Instructions
1. On the prompt of M1, enter 0 to send a **Gossip Announce** message to all of the connected peers. Enter `100` to assign the message a datatype. 

P1 will receive the message from M1, and broadcast it to all connected peers.
``` [23:11:09] INFO: Gossip Announce Msg Received from a GossipModule
[23:11:09] INFO: Sending Gossip Announce Message to: 127.0.0.3
[23:11:09] INFO: Sending Gossip Announce Message to: 127.0.0.2
``` 
P2, P3 will receive the **Gossip Announce** message from P1, and drop it.
``` [23:11:09] INFO: No Module has subscribed for Datatype 100, Dropping Message ```
