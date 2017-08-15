===============================================================================
          Procedure For Testing The Overlay Maintenance Of Peers
===============================================================================

Degree is the number of peers the current peer has to exchange information with.
There is a "max_connections" configuration parameter which defines the degree
of a peer.  

- If a peer's number of  connected peers are already equal to Degree, it should 
  drop any new connections

- If a peer's number of connected peers are less than degree, then it should 
  learn IPs of peers from neighboring peers and open new connections to them.

Our Solution:
------------

We have created a MaintainOverlay Thread in com.project.gossip.p2p package.
The purpose of MaintainOverlay thread is to gossip the IPs peers.
MaintainOverlay thread creates a PeerListMessage which contains the IPs of 
Peer's connected peers. This message is broadcasted by MaintainOverlay Thread
after every 10000 msec by default. This delay could also be controlled by 
setting the conf parameter "peer_list_send_delay=10000" in configuration file.



===================================================================================
                                  TEST Number 1
====================================================================================             
If a peer opens connection to another peer whose maximum connections(degree) is
already satisfied, the connection request should be dropped


We will create 3 peers for testing, 3 different conf files will be used

NOTE: Please follow the steps below in order to see the result!

Compile and package
--------------------
cd P2P-gossip/gossip
mvn clean package

Start BootStrap Server
----------------------
Note that switch -b specifies that it is a bootstrap server

Command:
java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.1.conf -b

------
PEER 1
------
Peer 1 is allowed to connect to only one peer

Change the max_connections value in config/gossip_127.0.0.1.conf file to 1

[gossip]
cache_size = 50
max_connections = 1
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.1:6001
api_address = 127.0.0.1:7001
log_level = FINE
peer_list_send_delay = 10000

Command:
java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.1.conf 

------
PEER 2
------
Peer 2 is allowed to connect to only one peer

Change the max_connections value in config/gossip_127.0.0.2.conf file to 1

[gossip]
cache_size = 50
max_connections = 1
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.2:6001
api_address = 127.0.0.2:7001
log_level = FINE
peer_list_send_delay = 10000

Command:
java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.2.conf

After you run this command, peer 2 will get peer 1's IP from bootstrap server 
and connect to it. Peer 1 and Peer 2 output will now show 1 connected peer.
At this point peer 1 and 2 have their degrees fulfilled (1 max_connection).

-------
Peer 3
-------

Now our goal is to start peer 3 with degree 2. When we will start peer 3, it will
get IPs of Peer 1 and Peer 2 from bootstrap server, it will try to open connections
to Peer 1 and Peer 2, but their max number of connections are 1 therefore they will
drop Peer 3's connection request.

Change the max_connections value in config/gossip_127.0.0.3.conf file to 2

[gossip]
cache_size = 50
max_connections = 2
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.3:6001
api_address = 127.0.0.3:7001
log_level = FINE
peer_list_send_delay = 10000

Command:
java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.3.conf

Peer 1 and Peer 2 will show output 
[21:36:08] INFO: Incoming Connection Request Denied, connected peers size 1 is equal to degree 1


Peer 3 will show output
[21:36:08] INFO: Trying to connect to: 127.0.0.2
[21:36:08] INFO: Trying to connect to: 127.0.0.1
[21:36:08] INFO: Size of Connected Peers: 0
[21:36:08] INFO: Connection Request denied by peer
[21:36:08] INFO: Connection Request denied by peer


STOP ALL PROCESSES BEFORE STARTING TEST 2, INCLUDING BOOTSTRAP SERVER

====================================================================================
                                  TEST 2
====================================================================================

Peers learn about new peers from gossiping, then open connections to them if
the number of their connected peers is less than their max_connections(degree)


Compile and package
--------------------
cd P2P-gossip/gossip
mvn clean package

Start BootStrap Server
----------------------
Note that switch -b specifies that it is a bootstrap server

Command:
java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.1.conf -b

------
PEER 1
------
Peer 1 is allowed to connect to only one peer

Change the max_connections value in config/gossip_127.0.0.1.conf file to 1

[gossip]
cache_size = 50
max_connections = 1
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.1:6001
api_address = 127.0.0.1:7001
log_level = FINE
peer_list_send_delay = 10000

Command:
java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.1.conf

------
PEER 2
------
Peer 2 is allowed to connect two peers

Change the max_connections value in config/gossip_127.0.0.2.conf file to 2

[gossip]
cache_size = 50
max_connections = 2
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.2:6001
api_address = 127.0.0.2:7001
log_level = FINE
peer_list_send_delay = 10000

Command:
java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.2.conf

After you run this command, peer 2 will get peer 1's IP from bootstrap server
and connect to it. Peer 1 and Peer 2 output will now show 1 connected peer.
At this point peer 1's degree is fullfilled but Peer 2 can connect to one more peer.



-------
Peer 3
-------

Now our goal is to start peer 3 with degree 1. When we will start peer 3, it will
get IPs of Peer 1 and Peer 2 from bootstrap server, it will try to open connections
to with Peer 1 or Peer 2 (note that bootstrap server randomizes the list of IPs).
If Peer 3 tries to connect to Peer 1 connection will be dropped, but it will be 
successfull in connecting to Peer 2

Change the max_connections value in config/gossip_127.0.0.3.conf file to 1

[gossip]
cache_size = 50
max_connections = 1
bootstrapper = 127.0.0.1:6001
listen_address = 127.0.0.3:6001
api_address = 127.0.0.3:7001
log_level = FINE
peer_list_send_delay = 10000

Command:
java -cp target/gossip-1.0-SNAPSHOT-jar-with-dependencies.jar com.project.gossip.Peer -c config/gossip_127.0.0.3.conf

Now wait for 5 to 10 secs, Peer 1 will receive PeerListMessage from Peer 2
and Peer 1 will learn about the IP of Peer 3. 

Similarly Peer 3 learns IP of Peer 1 from Peer 2.

- Kill Peer 2

Now Peer 1 and Peer 3 will establish a connection. Both of them know about each 
other's IP, either peer 1 or peer 3 will initiate a connection. 
