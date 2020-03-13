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
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(localPath));

            int len = Integer.parseInt(reader.readLine());
            byte[] bytes = new byte[len];

            bytesRead = in.read(bytes, 0, bytes.length);
            current = bytesRead;

            do {
                bytesRead = in.read(bytes, current, (bytes.length - current));
                if (bytesRead >= 0)
                    current += bytesRead;
            } while (bytesRead > -1);

            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();
            System.out.println("File " + hostPath + " downloaded to " + localPath + " (" + bytes.length + ")");

            fileOutputStream.close();
            in.close();
            reader.close();
            socket.close();

        } catch (UnknownHostException e) {
            System.err.println("Server not found at: " + host + ":" + port);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
