package Client.Threads.Request;

import Client.Client;
import Client.Util.Ports;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestServer implements Runnable {
    private ServerSocket serverSocket;
    private Client client;
    private volatile boolean stop;

    public RequestServer(Client client) throws IOException {
        this.serverSocket = new ServerSocket(Ports.requestPort);
        this.client = client;
        this.stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Socket socket = serverSocket.accept();
                Thread sendRequestedFileThread = new SendRequestedFileThread(socket, client.getLocalDir());
                sendRequestedFileThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        stop = true;
    }
}
