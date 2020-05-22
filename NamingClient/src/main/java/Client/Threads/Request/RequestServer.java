package Client.Threads.Request;

import Client.Client;
import Client.Util.Ports;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This server will check incoming attempts to send files to the node and create a SendReplicateFileThread for each incoming connection
 */
public class RequestServer implements Runnable {
    private ServerSocket serverSocket;  //The socket that will create new sockets for P2P communication
    private Client client;              //The client, for getting the correct directories
    private volatile boolean stop;      //A boolean to stop the thread

    /**
     * Constructor
     *
     * @param client the instance of client
     * @throws IOException If the replicatePort is already used
     */
    public RequestServer(Client client) throws IOException {
        this.serverSocket = new ServerSocket(Ports.requestPort);
        this.client = client;
        this.stop = false;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        while (!stop) {
            try {
                Socket socket = serverSocket.accept();
                Thread sendRequestedFileThread = new SendRequestedFileThread(socket, client.getRequestDir());
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

        System.out.println(Thread.currentThread().getName() + " Ended. (ReqServ)");
    }

    /**
     * Stop the thread
     */
    public void stop() {
        stop = true;
    }
}