import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {
    final static int ARGUMENT_IP = 1;
    final static String MESSAGE = "some text";
    final static String IP_ADDR = "228.5.6.7";
    final static int PORT = 7777;
    final static int BUFSIZE = 1024;

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
                            Thread.sleep(1000);
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
                        System.out.println("\n--------------------------------");
                        for (Map.Entry<String, Long> val : tmp.entrySet()) {
                            Date date = new Date();
                            Long currentTime = date.getTime();
                            if (val.getValue() + 7000 < currentTime) connections.remove(val.getKey());
                            else System.out.println(val.getKey());
                        }
                        System.out.println("--------------------------------\n");
                        todo.clear();
                        try {
                            Thread.sleep(2000);
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
}
