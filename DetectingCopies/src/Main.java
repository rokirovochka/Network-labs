import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public final static int ARGUMENT_IP = 1;
    public final static String MESSAGE = "some text";
    public static final int TIME_GAP = 7000;
    public static final int CLEANER_SLEEP_TIME = 2000;
    public static final int SENDER_SLEEP_TIME = 1000;
    public final static String IP_ADDR = "228.5.6.7";
    public final static int PORT = 7777;
    public final static int BUFSIZE = 1024;

    public static void main(String[] args) {
        String ip_addr = IP_ADDR;
        final Long startDate = (new Date()).getTime();

        if (args.length >= 2) {
            ip_addr = args[ARGUMENT_IP];
        }

        try {
            Map<String, Long> connections = new HashMap<String, Long>(), todo = connections;
            MulticastSocket server, socket;
            InetAddress group = InetAddress.getByName(IP_ADDR);
            server = new MulticastSocket(PORT);
            server.joinGroup(group);
            socket = new MulticastSocket();
            socket.joinGroup(group);
            Runnable receiver = new Runnable() {
                @Override
                public void run() {
                    byte[] buf = new byte[BUFSIZE];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    while (true) {
                        try {
                            server.receive(recv);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        todo.put(recv.getSocketAddress().toString(), (new Date()).getTime());
                    }
                }
            };

            Runnable sender = new Runnable() {
                @Override
                public void run() {
                    DatagramPacket packet = new DatagramPacket(MESSAGE.getBytes(), MESSAGE.length(), group, PORT);
                    while (true) {
                        try {
                            socket.send(packet);
                            Thread.sleep(SENDER_SLEEP_TIME);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            Runnable cleaner = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        connections.putAll(todo);
                        Map<String, Long> tmp = connections;
                        System.out.println("\n" + SEPARATOR);
                        for (Map.Entry<String, Long> val : tmp.entrySet()) {
                            Date date = new Date();
                            Long currentTime = date.getTime();
                            if (val.getValue() + TIME_GAP < currentTime) connections.remove(val.getKey());
                            else System.out.println(val.getKey());
                        }
                        System.out.println(SEPARATOR + "\n");
                        todo.clear();
                        try {
                            Thread.sleep(CLEANER_SLEEP_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            (new Thread(sender)).start();
            (new Thread(receiver)).start();
            (new Thread(cleaner)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String SEPARATOR = "--------------------------------";
}
