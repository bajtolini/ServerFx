package serverfx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerFx implements Runnable {

    private ServerThread clients[] = new ServerThread[50];
    private ServerSocket server = null;
    private Thread thread = null;
    private int clientCount = 0;

    public ServerFx(int port) {
        try {
            InetAddress adress = InetAddress.getLocalHost();
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port, 50, adress);
            System.out.println("Server started: " + server);
            start();
        } catch (IOException ioe) {
            System.err.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        while (thread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch (IOException ioe) {
                System.out.println("Server accept error: " + ioe);
                stop();
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread = null;
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void handle(int ID, String input) {
        for (int i = 0; i < clientCount; i++) {
            clients[i].send(input);
        }
    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ServerThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;
            try {
                toTerminate.close();
            } catch (IOException ioe) {
                System.err.println("Error closing thread: " + ioe);
            }
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ServerThread(this, socket);
            try {
                clients[clientCount].open(clientCount);
                clients[clientCount].start();
                clientCount++;
            } catch (IOException ioe) {
                System.err.println("Error opening thread: " + ioe);
            }
        } else {
            System.out.println("Client refused: maximum " + clients.length + " reached.");
        }
    }

    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("Usage: wrong port, type once more");
        } else {
            ServerFx server = new ServerFx(Integer.parseInt(args[0]));
        }
    }
}