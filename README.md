odesk_client_server
===================
1. One Server and many clients in the network.
2. When Client starts, it connects to the server (if there is a connection failure or server restart, clients should connect back to the server when it comes online. You can keep checking every 5seconds from the client)
3. From the server console, I should be able to send a message to any client that is connected. I should select a client and send a Java object. Command line is fine.
4. From the server, I should instruct 2 clients to exchange a message directly (again a java object) with each other. In this case, clients send messages to each other with out the message going through the server. If one or both of the clients needs to go into server mode to exchange message directly, you should start a new thread and make it happen. After the successful message exchange, both clients should send a "success" message (another java object) to the server. Server just prints it in the console.

Using Proto-bufs to exchange messages and convert them to java objects after receiving it on the server or client.
