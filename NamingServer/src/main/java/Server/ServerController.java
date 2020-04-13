package Server;

import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class ServerController {

    private Server server = new Server("map.xml");

    // POST: register the node
    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String ip) {
        try {
            return server.registerNode(name, InetAddress.getByName(ip));
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        return "Register failed! IP not found!";
    }

    // GET: location of file
    @GetMapping("/file")
    public String getFileLocation(@RequestParam String filename) {
        return server.fileLocation(filename).toString().substring(1);
    }

    // DELETE: delete the node
    @DeleteMapping("/unregister")
    public void unregister(@RequestParam String name) {
        server.unregisterNode(name);
    }
}
