import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

/*
    Communication setup:

        Client                      Server
           |         filePath         |
           |  --------------------->  |
           |                          |
           |         fileSize         |
           |  <---------------------  |
           |                          |
           |           file           |
           |  <---------------------  |
           |                          |

 */

public class Client {
    public static void main(String[] args) {
        // Test for correct amount of inputs
        if (args.length != 4) return;

        // Load inputs to variables
        InetAddress host = null;
        try {
            host = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        int port = Integer.parseInt(args[1]);
        String hostPath = args[2];
        String localPath = args[3];
        
        System.out.println("Connecting to: " + host + ":" + port);
        System.out.println("Copying " + hostPath);
        System.out.println("To " + localPath);

        BufferedOutputStream fileOutputStream;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        byte[] buff = null;

        try {
            buff = hostPath.getBytes();
            DatagramPacket requestPacket = new DatagramPacket(buff, buff.length, host, port);
            socket.send(requestPacket);

            byte[] receive = new byte[65535];
            DatagramPacket fileSizePacket = new DatagramPacket(receive, receive.length);
            socket.receive(fileSizePacket);
            ByteBuffer wrapped = ByteBuffer.wrap(receive);
            int len = wrapped.getInt();

            receive = new byte[len];
            DatagramPacket filePacket = new DatagramPacket(receive, receive.length);
            socket.receive(filePacket);

            // Create an outputstream to write files to the socket
            fileOutputStream = new BufferedOutputStream(new FileOutputStream(localPath));

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(filePacket.getData(), 0, filePacket.getData().length);
            fileOutputStream.flush();
            System.out.println("File " + hostPath + " downloaded to " + localPath + " (" + filePacket.getData().length + ")");

        } catch (UnknownHostException e) {
            System.err.println("Server not found at: " + host + ":" + port);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            e.printStackTrace();
        }
    }
}
