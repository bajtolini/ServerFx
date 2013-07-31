package serverfx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread extends Thread {

    private boolean stop;
    private ServerFx server = null;
    private Socket socket = null;
    private int ID = 0;
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;
    private int clientCount;


    public ServerThread(ServerFx server, Socket socket) {
        super();
        this.stop = false;
        this.server = server;
        this.socket = socket;
        ID = this.socket.getPort();
    }

    public void send(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.err.println(ID + " ERROR sending: " + ioe.getMessage());
            server.remove(ID);
        }
    }

    public int getID() {
        return ID;
    }

    public void run() {
        clientCount++;
        System.out.println("Server Thread " + ID + " running.");
        if (clientCount == 1) {
            send("Welcome to Chat, currently: " + clientCount + " user online");
        } else {
            send("Welcome to Chat, currently: " + clientCount + " users online");
        }
        while (!this.stop) {
            try {
                server.handle(ID, streamIn.readUTF());
            } catch (IOException ioe) {
                System.err.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                break;
            }
        }
    }

    public void open(int clients) throws IOException {
        streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        clientCount = clients;
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (streamIn != null) {
            streamIn.close();
        }
        if (streamOut != null) {
            streamOut.close();
        }
    }
    
    public void finish() {
        this.stop = true;
    }
}