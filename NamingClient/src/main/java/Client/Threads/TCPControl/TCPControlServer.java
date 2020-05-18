package Client.Threads.TCPControl;

import Client.Client;
import Client.Util.Ports;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPControlServer implements Runnable{
    private ServerSocket serverSocket;
    private Client client;
    private volatile boolean stop;

    public TCPControlServer(Client client) throws IOException {
        this.serverSocket = new ServerSocket(Ports.tcpControlPort);
        this.client = client;
        this.stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Socket socket = serverSocket.accept();
                Thread tcpControlThread = new TCPControlThread(socket, client);
                tcpControlThread.start();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

        }
    }

    public void stop() {
        stop = true;
    }
}
