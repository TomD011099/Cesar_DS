package Client.Threads.TCPControl;

import Client.Client;
import Client.Util.Ports;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The thread that monitors incoming traffic for TCPControl and creates thread to handle these communications
 */
public class TCPControlServer implements Runnable {
    private ServerSocket serverSocket;  //The serversocket that'll accept all incoming connections
    private Client client;              //The client to give along with the threads
    private volatile boolean stop;      //A boolean to stop the thread

    /**
     * Constructor
     *
     * @param client The instance of client
     * @throws IOException If the tcpControlPort is invalid/in use
     */
    public TCPControlServer(Client client) throws IOException {
        this.serverSocket = new ServerSocket(Ports.tcpControlPort);
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
                Thread tcpControlThread = new TCPControlThread(socket, client);
                tcpControlThread.start();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

        }

        System.out.println(Thread.currentThread().getName() + " Ended. (TCPServ)");
    }

    /**
     * Stop the thread
     */
    public void stop() {
        stop = true;
    }
}
