# Demo Video Link - [Video]([https://uflorida-my.sharepoint.com/personal/suryateja_paduri_ufl_edu/_layouts/15/stream.aspx?id=%2Fpersonal%2Fsuryateja%5Fpaduri%5Fufl%5Fedu%2FDocuments%2FFinal%20video%2Emov&ga=1](https://uflorida-my.sharepoint.com/:v:/g/personal/suryateja_paduri_ufl_edu/EdaPI8Fj-ZFOuN9wgmHpsewBtpALhAZspHDejGYb50ZsJw?e=jQNhTd))


# P2P File Sharing
This peer-to-peer file-sharing program uses TCP protocol connections and operates on a similar theory to BitTorrent's choking and unchoking mechanisms.

In this project, we have a collection of independent computers acting as a distributed repository of files, with each machine holding a portion of the file we want to transfer.

# Team Members: 
1. Surya Teja Paduri  
   Contributed in making TCP connections between the peers.
   Worked on establishing Handshake protocol.
2. Venkata Vynatheya  
   Worked on initiating the File Exchange protocol.
   Contributed in choking and unchoking mechanisms of the peers.
3. Lalith Phani Srinivas Kandregula   
   Worked on communication between the peers whilst the data exchange.
   Also logging the messages during the file exchange process.

# Protocol
These computers, which function as peers, look for and download the necessary file's missing pieces by contacting the participating peer who has them and distributing the ones they already have.
 We employ the TCP protocol to establish connections between peers in our project who want to share files with one another.
The handshake message, which consists of the header, zero bits, and peer ID, is sent by the peers to each other before they can share files.
A stream of data messages comprised of the message length, type, and the payload is transmitted.
There are various kinds of payloads and message exchanges between peers. The following are different  message kinds:
1) CHOKE
2) UNCHOKE
3) BITFIELD
4) HAVE
5) INTERESTED
6) NOT INTERESTED
7) PIECE
8) REQUEST

# Functioning of File Transfer: 
StartRemotePeers launch the peers in the sequence supplied in the PeerInfo config file, and the peer process accepts the peer ID as input.
The newly established peer must establish TCP connections with all previously established peers participating in file sharing.
All peers also read the standard configuration file, which provides information about the shared file, including its size, choking and unchoking intervals, and the number of preferred neighbours.
Inside the PeerInfo file, we can see that the last column has 0's and 1's. This number denotes whether the peer has the entire file with it. Once the whole execution is done, the PeerInfo.cfg is modified, and we can observe that the last column would change to 1s for all the peers since the file transfer is successful.
Initially, there were no peers for the file. Hence the first peer which is launched will just listen and wait for the other peers to start and make the connection. Unless another peer has started, the first peer will just wait on the given port.
Additionally, we keep track of each peer's TCP connections with other peers, changes to their preferred neighbours, changes to their optimistically unchoked neighbours, also the times at which they are choked and unchoked by their peers, have/interested/not interested messages they receive and times when they finish downloading a portion or the entire file.

# File Transfer
If a peer needs a file, it searches for it with the filename or another keyword and a hop count of one.

The search request is made to other peers in the overlay network who are within the current hop count or fewer of the asking peer, and it expires after a certain number of hop count seconds. The use of repeated search requests is prohibited.
If a peer with the necessary file receives the search request, it responds to the peer who made the original request. If the peer making the request gets the response, it consumes it; if not, it sends it to the peer making the original request.
If the requester receives a response, it accumulates all responses until the request's expiration date; all subsequent responses are disregarded. The asking peer then creates a TCP connection with the response from the peer that matches the necessary filename and piece index. After moving the files to their location, the requester peer updates as necessary. The TCP connection is cut off after it has received the file.
The peer should find again once the hop counts as increased by one if the search request ends unsuccessfully. Until the search request is successful or the hop count reaches a predetermined hop count threshold, the hop count should keep going up—the Protocol's termination.
It is advisable to terminate nodes if their number surpasses the maximum number of permitted hop counts. If a departing only has one neighbour, it simply cuts off the TCP connection with that neighbour. If it has more than one neighbour, it chooses one of them to be the neighbour of every other neighbour (unless they are already neighbours). Then, all of its TCP connections and active file transfers should be ended.


