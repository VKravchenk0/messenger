# messenger
Simple instant messenger

This project has been developed a long time ago <strike>in a galaxy far-far away</strike>. The code is awful <strong>but</strong> it works.
Technologies used in the project: Java SE, Swing, MySQL, Sockets, Asynchronous I/O

To start server on your computer you need to have MySQL server. The main class that runs the server is engine.Server class. 
In server setup window you should specify the database username and password and server ip address (127.0.0.1 to start on localhost or
your computer`s ip address to make server visible from internet) and port.

To start client you need to change SERVER_ADDRESS and SERVER_PORT in engine.ClientEngine.java. The main class is also engine.ClientEngine.
