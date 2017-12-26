import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Receiver {
    // I/O size for stream, this is to avoid creating too large direct byte buffer
    // internally in the JDK stream classes
    private static final int IO_SIZE = 128 * 1024;
    private static final int CHUNK_END_OFFSET = 134217600;
    private static final int READ_BUFFER_SIZE_THRESHOLD = 4194304;

    private static int readInputStreamToByteBuffer(InputStream inputStream,
                                                   ByteBuffer buffer,
                                                   int len)
            throws IOException {
        if (len == 0) {
            return 0;
        }

        int remaining = len;
        int total = 0;
        if (buffer.hasArray()) {
            while (remaining > 0) {
                int rlen = Math.min(remaining, IO_SIZE);
                int c = inputStream.read(buffer.array(),
                        buffer.arrayOffset() + buffer.position(), rlen);
                if (c == -1) { // end of stream
                    break;
                }
                total += c;
                remaining -= c;
                buffer.position(buffer.position() + c);
            }
        } else {
            // TODO: use Bits.copyToArray() to avoid double copy for direct buffer
            int buflen = Math.min(IO_SIZE, len);
            byte[] tmpBuffer = new byte[buflen];
            while (remaining > 0) {
                int rlen = Math.min(remaining, buflen);
                int c = inputStream.read(tmpBuffer, 0, rlen);
                if (c == -1) { // end of stream
                    break;
                }
                total += c;
                remaining -= c;
                buffer.put(tmpBuffer, 0, c);
            }
        }

        return total;
    }

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("HttpClient receiver started...............");
        while (true) {
            Socket socket = serverSocket.accept();
            String ip = socket.getInetAddress().toString();
            System.out.println("Client IP is： " + ip);

            /*//read inputStream
            InputStream inputStream = socket.getInputStream();

            ByteBuffer byteBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE_THRESHOLD);


            int readOffSet = 0;
            int endOffSet = CHUNK_END_OFFSET;
            while (readOffSet < endOffSet) {
                int readLen = Math.min(endOffSet - readOffSet, READ_BUFFER_SIZE_THRESHOLD);
                //readLen = Math.min(readLen, byteBuffer.remaining());
                System.out.println("readLen" + readLen);

                byteBuffer.clear();
                readInputStreamToByteBuffer(inputStream, byteBuffer, readLen);
                readOffSet += readLen;
            }*/

            /*InputStream inputStream = socket.getInputStream();
            int bytesRead = 0;
            int totalRead = 0;
            int remain = 0;
            byte b[] = new byte[READ_BUFFER_SIZE_THRESHOLD];
            while (bytesRead >= 0) {
                bytesRead = inputStream.read(b);
                totalRead += bytesRead;
                remain = CHUNK_END_OFFSET - totalRead;
            }*/

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

            //inputStream.close();
            socket.close();
            System.out.printf("Socket for %s closed.\n", ip);
        }
    }
}
