package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BootstrapThread extends Thread {

    private boolean concernsNext;
    private Client client;
    private ServerSocket serverSocket;

    BootstrapThread(boolean concernsNext, Client client) throws IOException {
        this.client = client;
        this.concernsNext = concernsNext;
        if (this.concernsNext)
            serverSocket = new ServerSocket(56789);
        else
            serverSocket = new ServerSocket(11111);
    }

    @Override
    public void run() {
        // TODO (maybe) check this
        try {
            Socket socket = serverSocket.accept();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Set the prevNode or nextNode depending on which ip is received
            if (concernsNext)
                client.setNext(bufferedReader.readLine(), socket.getInetAddress());
            else
                client.setPrev(bufferedReader.readLine(), socket.getInetAddress());

            bufferedReader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
