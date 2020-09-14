import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    static ExecutorService executorService = Executors.newFixedThreadPool(8);
    public static final int SERVER_PORT = 7777;
    public static final int PORT_ARGUMENT = 1;

    public static void main(String[] args) {

        int serverPort = SERVER_PORT;
        if (args.length >= 2) {
            serverPort = Integer.parseInt(args[PORT_ARGUMENT]);
        }

        try (ServerSocket server = new ServerSocket(serverPort)) {

            while (!server.isClosed()) {
                Socket client = server.accept();
                executorService.execute(new ClientHandler(client));
            }

            executorService.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
