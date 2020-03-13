import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        if (args.length != 4) return;

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String hostPath = args[2];
        String localPath = args[3];

        int bytesRead;
        int current;

        try {
            Socket socket = new Socket(host, port);

            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(localPath));

            writer.println(hostPath);

            int len = Integer.parseInt(reader.readLine());
            byte[] bytes = new byte[len];

            System.out.println("Total amount of bytes to read: " + bytes.length);

            bytesRead = in.read(bytes, 0, bytes.length);
            System.out.println("Read " + bytesRead + " bytes. Len = " + len);

            if (bytesRead != len) {
                current = bytesRead;
                System.out.println("More reads");
                do {
                    bytesRead = in.read(bytes, current, (bytes.length - current));
                    System.out.println("Read " + current + " bytes.");
                    if (bytesRead >= 0)
                        current += bytesRead;
                } while (current < len);
            }

            System.out.println("File received, downloading to " + localPath);

            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();
            System.out.println("File " + hostPath + " downloaded to " + localPath + " (" + bytes.length + ")");

            writer.println("Done.");

            fileOutputStream.close();
            in.close();
            reader.close();
            out.close();
            writer.close();
            socket.close();

        } catch (UnknownHostException e) {
            System.err.println("Server not found at: " + host + ":" + port);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
        }

        return;
    }
}
