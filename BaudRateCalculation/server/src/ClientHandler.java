import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    public static final String RECEIVE_FILES_PATH = "./uploads/";

    private static Socket client;
    private static String fileName;
    private static long fileSize = -1;

    DataOutputStream out;
    DataInputStream in;

    public ClientHandler(Socket client) {
        ClientHandler.client = client;
        try {
            out = new DataOutputStream(client.getOutputStream());
            in = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!client.isClosed()) {
                receiveFileInfo();
                receiveFile(RECEIVE_FILES_PATH + fileName);
            }

            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFileInfo() {
        try {
            while (!client.isClosed()) {
                if (in.available() > 0 && (fileName = in.readUTF()) != null) {
                    System.out.println("Server reading from channel");
                    System.out.println("READ from clientDialog file name - " + fileName);
                    break;
                }
            }
            while (!client.isClosed()){
                if (in.available() > 0 && (fileSize = in.readLong()) >= 0) {
                    System.out.println("Server reading from channel");
                    System.out.println("READ from clientDialog file size - " + fileSize);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveFile(String outputFilePath) {
        InputStream is;
        BufferedInputStream bis;
        FileOutputStream fos;
        BufferedOutputStream bos;
        File output;
        try {
            output = new File(outputFilePath);
            is = client.getInputStream();
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(output);
            bos = new BufferedOutputStream(fos);
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
