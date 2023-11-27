# Chat Program

A simple command-line chat application built in Java using socket connections and multithreading. This chat application allows 
multiple users to connect to a server and communicate by exchanging messages and sharing files.

## Installation

1. **Clone the Repository:**
    ```bash
    git clone https://github.com/Navnedia/Chat-Program.git
    cd Chat-Program
    ```

2. **Compile the Code:**
    ```bash
    javac ChatClient.java
    javac ChatServer.java
    ```
   
## Usage

1. **Start the Chat Server:** run the server by specifying your desired server listening port.
    ```bash
    java ChatServer <port>
    ```
   **Example:** `java ChatServer 6001`


2. **Connect Client to the Server:** on the client machine, run the `ChatClient` application.
    - `-l` to specify the local port on the client to receive file requests. 
    - `-p` the remote port of the chat server you wish to connect to.
    - `-s` (optional) the IP address of the chat server. Defaults to `localhost`.
   > **Note:** If you are running multiple servers or clients on the same host, make sure none of your listen ports conflict.
   ```bash
   java ChatClient -l <listening port number> -p <connect server port> [-s] [connect server address]
   ```
   **Example:** `java ChatClient -l 6002 -p 6001 -s localhost`


3. **Client Username:** you will be prompted to enter a username.

4. **Start Messaging:** Once connected, you will receive messages in the terminal, and you can
send your own messages using the `M` menu option.

5. **File Sharing:** Use a designated menu option `F` to request a file, then follow the prompts
to select, the user that owns the file, and the name of the file you want (a file in
the current working directory).

6. **Disconnect:** you may disconnect at any time by using the `X` menu option or by
terminating the program with `CTRL + Z` or `CTRL + C`.
