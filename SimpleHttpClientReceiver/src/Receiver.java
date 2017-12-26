import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("HttpClient receiver started...............");
        while (true) {
            Socket socket = serverSocket.accept();
            String ip = socket.getInetAddress().toString();
            System.out.println("Client IP is： " + ip);

            //read inputStream
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String readContent;
            while ((readContent = bufferedReader.readLine()) != null && !readContent.equals("###EOF")) {
                //System.out.println("ReadFromClient: " + readContent);
            }
            System.out.printf("Read data for %s finished.\n", ip);

            //返回response
            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write("HTTP/1.1 200 OK\n");
            bufferedWriter.write("Content-Length: 10\n");
            bufferedWriter.write("Content-Type: text/html;charset=utf8\n");
            bufferedWriter.write("\n"); // 区分HEAD区和正文区
            bufferedWriter.write("Data Received!");
            bufferedWriter.flush();
            bufferedWriter.close();
            System.out.printf("Response for %s sent.\n", ip);

            socket.close();
            System.out.printf("Socket for %s closed.\n", ip);
        }
    }
}
