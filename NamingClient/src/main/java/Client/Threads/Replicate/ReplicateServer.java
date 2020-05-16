package Client.Threads.Replicate;

import Client.Client;
import Client.Util.Ports;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReplicateServer implements Runnable {
    private ServerSocket serverSocket;
    private Client client;
    private volatile boolean stop;

    public ReplicateServer(Client client) throws IOException {
        this.serverSocket = new ServerSocket(Ports.replicatePort);
        this.client = client;
        this.stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Socket socket = serverSocket.accept();
                Thread receiveReplicationFileTread = new ReceiveReplicateFileThread(socket, client.getReplicaDir(), client.getLocalFileSet(), client.getPrevNode());
                receiveReplicationFileTread.start();
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
