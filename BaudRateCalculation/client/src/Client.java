import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    //public static final String EMPTY_STRING = "";
    // public static final String DEFAULT_IP_ADDR = "127.0.0.1";
    // public static final int DEFAULT_PORT = 7777;
    public static final int PORT_ARGUMENT = 2;
    public static final int IP_ADDR_ARGUMENT = 1;
    public static final int FILE_PATH_ARGUMENT = 0;

    private static Socket socket;
    private static String inputFilePath;
    private static String ipAddr;
    private static int port;

    public static void main(String[] args) {

        if (args.length >= 3) {
            inputFilePath = args[FILE_PATH_ARGUMENT];
            ipAddr = args[IP_ADDR_ARGUMENT];
            port = Integer.parseInt(args[PORT_ARGUMENT]);
        } else {
            System.out.println("Expected arguments: [file path] [ip address] [port]");
            System.exit(0);
        }

        try {
            socket = new Socket(ipAddr, port);
            sendFileInfo();
            sendFile();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void sendFileInfo() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            if (!socket.isOutputShutdown()) {
                out.writeUTF(getFileNameFromFilePath(inputFilePath));
                out.writeLong((new File(inputFilePath)).length());
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileNameFromFilePath(String filePath) {
        String res = "";
        for (int i = filePath.length() - 1; i >= 0; i--) {
            if (filePath.charAt(i) == '\\' || filePath.charAt(i) == '/') {
                break;
            }
            res += filePath.charAt(i);
        }
        StringBuffer fileName = new StringBuffer(res);
        fileName.reverse();
        return String.valueOf(fileName);
    }

    private static void sendFile() {
        FileInputStream fis;
        BufferedInputStream bis;
        OutputStream os;
        BufferedOutputStream bos;
        try {
            File input = new File(inputFilePath);
            fis = new FileInputStream(input);
            bis = new BufferedInputStream(fis);
            os = socket.getOutputStream();
            bos = new BufferedOutputStream(os);
            byte[] buffer = new byte[1024];
            int data;
            while (true) {
                data = bis.read(buffer);
                if (data != -1) {
                    bos.write(buffer, 0, 1024);
                } else {
                    bis.close();
                    bos.close();
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
