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
log_level = INFO
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
log_level = INFO
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
log_level = INFO
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
1. On the prompt of M1, enter `0` to send a **Gossip Announce** message to all of the connected peers. Enter `100` to assign the message a datatype. 

P1 will receive the message from M1, and broadcast it to all connected peers.
``` [23:11:09] INFO: Gossip Announce Msg Received from a GossipModule
[23:11:09] INFO: Sending Gossip Announce Message to: 127.0.0.3
[23:11:09] INFO: Sending Gossip Announce Message to: 127.0.0.2
``` 
P2, P3 will receive the **Gossip Announce** message from P1, and drop it.
``` [23:11:09] INFO: No Module has subscribed for Datatype 100, Dropping Message ```

2. On the prompt of M3, enter `1` to send a **Gossip Notify** message. Enter `100` to subscribe M3 to this datatype.

```
Gossip Notify Message Sent!
Waiting for Gossip Notification Message of datatype 222 ```

P3 will receive the message from M3, and forward it any new messages of the datatype `100`
```[23:19:10] INFO: Gossip Notify Msg Received from a Gossip Module```


3. On the prompt of M1, enter `0`, and then `100` to send a **Gossip Announce** message to all connected peers.

Like before, P1 will receive this message, and broadcast it to other nodes.
P3 will receive the **Gossip Announce** message from P1. It will send a **Gossip Notification** to M3.
```
[23:24:17] INFO: Gossip Announce Msg Received from: 127.0.0.1
[23:24:17] INFO: Sending Gossip Notification to module
```
M3 will receive the **Gossip Notification** message
```
Gossip Notification Received for datatype 100 data length 13
Press 1 to send validation or Press 0 to send invalidation for message id 0 : 
```

4. On the prompt of M3, enter `1` to send a **Gossip Validation** message signifying the validity of the message.

P3 receives the message, and checks for its validity
```
[23:26:37] INFO: Gossip Validation Msg Received from a Gossip Module
[23:26:37] INFO: Gossip Validation Msg Valid
```

If it's valid, it will broadcast the **Gossip Announce** message it received in 3 to the other two peers.
```
[23:36:38] INFO: Gossip Announce Msg Received from: 127.0.0.3
[23:36:38] INFO: No Module has subscribed for Datatype 100, Dropping Message
```
In the case it's not valid, no broadcast will occur, and the message will simply be discarded by P3.
