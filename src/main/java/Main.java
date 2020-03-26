import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        Server s = new Server("map.xml");
        s.registerNode("Test", InetAddress.getByName("192.168.1.1"));
        s.registerNode("Test2", InetAddress.getByName("192.168.1.2"));

        s.clearMap();

        s.loadMap();
    }
}
