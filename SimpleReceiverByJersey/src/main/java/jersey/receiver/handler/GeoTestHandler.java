package jersey.receiver.handler;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Path("/geotest")
public class GeoTestHandler {
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

    @POST
    public Response handleGeoTestRequest(@Context HttpServletRequest request)
    {
        InputStream inputStream = null;
        try {
            //read inputStream
            inputStream = request.getInputStream();
            ByteBuffer byteBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE_THRESHOLD);
            int readOffSet = 0;
            int endOffSet = CHUNK_END_OFFSET;
            while (readOffSet < endOffSet) {
                int readLen = Math.min(endOffSet - readOffSet, READ_BUFFER_SIZE_THRESHOLD);
                //readLen = Math.min(readLen, byteBuffer.remaining());
                //System.out.println("readLen" + readLen);

                byteBuffer.clear();
                readInputStreamToByteBuffer(inputStream, byteBuffer, readLen);
                readOffSet += readLen;
            }
            System.out.println("Total read length: " + readOffSet);

            return Response.ok().build();
        } catch (IOException e) {
            System.out.println("Failed to handle input stream." + e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                System.out.println("Failed to clean up input stream." + e);
            }
        }
    }
}