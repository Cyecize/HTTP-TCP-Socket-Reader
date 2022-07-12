
# HTTP-TCP-Socket-Reader
Materials from a YouTube video on 'How to properly read content from TCP socket' featuring a simple HTTP server that can serve and save files.

Link to video on YouTube:
https://www.youtube.com/watch?v=eEH2aYJI7ss

# Project workflow
1. Listens for incoming connections using ServerSocket on a given port.
2. Once a client has connected, a new Thread is spawned and the client Socket is processed there.
3. The HTTP content coming from the TCP socket is ready '**byte-by-byte**' until the **CRLF** before the request body is reached, then **METHOD, URL and HEADERS** are extracted from that text.
4. If there is a request body, the data is read with a buffer of 2KB and it is read until the same amount of bytes are read as specified in the '**Content-Length**' header. This is the most important lesson of this project and the video about it. **TCP sockets are asynchronous by nature** so we need to know how much we need to read. InputStream.available() **DOES NOT WORK** for input streams coming from TCP sockets.
5. Based on GET / POST a file is server or a file is uploaded to the server.

# Interested in Web Servers?
Check out my other project, Javache Web Server. A fully functional Web Server written from zero, just like this project.
https://github.com/Cyecize/Java-Web-Server
