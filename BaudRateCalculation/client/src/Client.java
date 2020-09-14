import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static final String IP_ADDR = "127.0.0.1";
    public static final int PORT_ARGUMENT = 3;
    public static final int IP_ADDR_ARGUMENT = 2;
    public static final int FILE_PATH_ARGUMENT = 1;

    public static void main(String[] args) {
        String ipAddr = IP_ADDR;
        int port = 7777;
        String filePath;
        if (args.length >= 4) {
            filePath = args[FILE_PATH_ARGUMENT];
            ipAddr = args[IP_ADDR_ARGUMENT];
            port = Integer.parseInt(args[PORT_ARGUMENT]);
        } else {
            System.out.println("Expected arguments: [file path] [ip address] [port]");
            System.exit(0);
        }

        try (Socket socket = new Socket(ipAddr, port)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            while (!socket.isOutputShutdown()) {
                out.writeUTF("213");
                out.flush();
                break;
            }
            out.close();
            in.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
