import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;

public class ClientHandler implements Runnable {

    public static final String RECEIVE_FILES_PATH = "./uploads/";
    public static final long DELTA = 3000;
    private static Socket client;
    private static String fileName;
    private static long fileSize = -1;

    private static DataOutputStream out;
    private static DataInputStream in;

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
                checkReceivedFile();
            }

            in.close();
            out.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkReceivedFile() {
        final String OK = "Everything is ok, file received successfully!";
        final String BAD = "Something went wrong,file wasnt received correctly";
        String textToSend;
        if (fileSize == (new File(RECEIVE_FILES_PATH + fileName)).length()) {
            textToSend = OK;
        } else {
            textToSend = BAD;
        }
        if (!client.isOutputShutdown()) {
            try {
                out.writeUTF(textToSend);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveFileInfo() {
        try {
            while (!client.isClosed()) {
                if (in.available() > 0 && (fileName = in.readUTF()) != null) {
                    System.out.println("Server reading from channel");
                    System.out.println("READ from client file name - " + fileName);
                    break;
                }
            }
            while (!client.isClosed()) {
                if (in.available() > 0 && (fileSize = in.readLong()) >= 0) {
                    System.out.println("Server reading from channel");
                    System.out.println("READ from client file size - " + fileSize / 1024 + "KB");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String outputFilePath) {
        BufferedInputStream bis;
        FileOutputStream fos;
        BufferedOutputStream bos;
        File output;
        try (InputStream is = client.getInputStream()) {
            output = new File(outputFilePath);
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(output);
            bos = new BufferedOutputStream(fos);
            byte[] buffer = new byte[1024];
            int data;

            double endTime, startTime = System.currentTimeMillis();
            double lastTime = startTime, currentTime = startTime;
            double kBytesCount = 0, kBytesCountPrev = 0;
            double speed, avgSpeed;
            DecimalFormat dec = new DecimalFormat("#0.00");
            while (true) {
                data = bis.read(buffer);
                if (data != -1) {
                    bos.write(buffer, 0, 1024);
                    kBytesCount++;
                    currentTime = System.currentTimeMillis();

                    if (currentTime - DELTA >= lastTime) {
                        lastTime = currentTime;
                        speed = (kBytesCount - kBytesCountPrev) / (DELTA / 1000);
                        avgSpeed = kBytesCount / ((currentTime - startTime) / 1000);
                        System.out.println("Current speed: " + dec.format(speed) + "KB/s");
                        System.out.println("Average speed: " + dec.format(avgSpeed) + "KB/s\n");
                        kBytesCountPrev = kBytesCount;
                    }
                } else {
                    endTime = System.currentTimeMillis();
                    avgSpeed = kBytesCount / ((endTime - startTime) / 1000);
                    System.out.println("Average speed: " + dec.format(avgSpeed) + "KB/s\n");

                    checkReceivedFile();
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
