import java.io.*;
import java.net.*;
import java.sql.SQLOutput;

public class Server {
    public static void main(String[] args) {
        if (args.length != 1) return;

        int port = Integer.parseInt(args[0]);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server online on port " + port);

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                OutputStream out = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(out, true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String fileName = reader.readLine();

                File file = new File(fileName);
                if (!file.exists()) {
                    System.err.println(fileName + "does not exist.");
                    return;
                }
                byte[] bytes = new byte[(int)file.length()];
                BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
                fileInputStream.read(bytes, 0, bytes.length);

                writer.println(bytes.length);

                System.out.println("Sending " + fileName + " (" + bytes.length + " bytes)");
                out.write(bytes, 0, bytes.length);
                out.flush();
                System.out.println("Done.");

                fileInputStream.close();
                writer.close();
                reader.close();
                out.close();
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
