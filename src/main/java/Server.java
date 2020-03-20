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

public class Server {
    public static void main(String[] args) {
        // Test for correct amount of inputs
        if (args.length != 1) return;

        // Parse the portnumber
        int port = Integer.parseInt(args[0]);
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.println("Server online on port " + socket.getPort() + "\n");

        while (socket != null) {
            try {
                // Read the path to the file that has to be copied
                byte[] receive = new byte[65535];
                DatagramPacket fileNamePacket = new DatagramPacket(receive, receive.length);
                socket.receive(fileNamePacket);
                String fileName = new String(fileNamePacket.getData());
                fileName = fileName.replaceAll("\\u0000", "");
                System.out.println("File " + fileName + " requested.");

                InetAddress clientAddr = fileNamePacket.getAddress();
                int clientPort = fileNamePacket.getPort();

                // Check if the file exists
                File file = new File(fileName);
                if (!file.exists()) {
                    System.err.println(fileName + " does not exist.");
                    //TODO maybe let Client know something went wrong
                    return;
                } else
                    System.out.println("File " + fileName + " found, starting transfer.");

                // Make an array of bytes and store the file in said array
                byte[] fileData = new byte[(int) file.length()];
                BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
                fileInputStream.read(fileData, 0, fileData.length);

                if (fileData.length > 65535) {
                    System.err.println("File size too big.");
                    //TODO maybe let Client know something went wrong
                    return;
                }

                // Let the client know how much bytes will be sent
                byte[] fileLen = ByteBuffer.allocate(4).putInt(fileData.length).array();
                DatagramPacket fileLengthPacket = new DatagramPacket(fileLen, fileLen.length, clientAddr, clientPort);
                socket.send(fileLengthPacket);

                // Send the bytes
                System.out.println("Sending " + fileName + " (" + fileData.length + " bytes)");
                DatagramPacket filePacket = new DatagramPacket(fileData, fileData.length, clientAddr, clientPort);
                socket.send(filePacket);

            } catch (IOException e) {
                System.err.println("Server error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
