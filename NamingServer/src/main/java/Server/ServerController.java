package Server;

import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class ServerController {

    private Server server = new Server("map.xml");

    // TODO can be removed because of discovery
    /**
     * POST: register the node
     *
     * @param name The new name of the node
     * @param ip   The ip address of the node
     * @return 1: all good,
     * -1: hash already exists,
     * -2: host not found
     */
    @PostMapping("/register")
    public int register(@RequestParam String name, @RequestParam String ip) {
        try {
            return server.registerNode(name, InetAddress.getByName(ip));
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        return -2;
    }

    /**
     * GET: location of file
     *
     * @param filename The name of the requested file
     * @return The ip address of the node that stores the file
     */
    @GetMapping("/file")
    public String getFileLocation(@RequestParam String filename) {
        return server.fileLocation(filename).toString().substring(1);
    }

    /**
     * DELETE: delete the node
     *
     * @param name the name of the node that has to be deleted
     */
    @DeleteMapping("/unregister")
    public void unregister(@RequestParam String name) {
        server.unregisterNode(name);
    }
}
