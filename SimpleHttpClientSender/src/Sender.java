import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.nio.charset.Charset;

public class Sender {

    private static void sendGet() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://127.0.0.1:9999");
        //HttpGet httpGet = new HttpGet("http://www.google.com/search?hl=en&q=httpclient&btnG=Google+Search&aq=f&oq=");
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            try {
                //获得起始行
                System.out.println(response.getStatusLine().toString());
                //获得首部
                Header[] hs = response.getAllHeaders();
                for (Header h : hs) {
                    System.out.println(h.getName() + ":\t" + h.getValue() + "\n");
                }
                //获取实体，方法1：EntityUtils
                System.out.println("从服务器端获取的内容为：\n" + EntityUtils.toString(entity));
                ///获取实体，方法2：EntityUtils
                /*BufferedReader rd = new BufferedReader(
                        new InputStreamReader(entity.getContent()));
                String line;
                while((line = rd.readLine()) != null) {
                    System.out.println(line);
                }*/
                EntityUtils.consume(entity);
            } finally {
                response.close();
                httpClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendPost(String requsetIP, String filePath) {
        //requsetIP = "127.0.0.1";
        //filePath = "C:\\ECS\\Esca\\Nolan_178";
        for (int loop = 1; loop <= 10; loop++) {
            long startTime = System.nanoTime();
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost("http://" + requsetIP + ":9999");
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
            try {
                String customEOF = "###EOF";
                InputStream entityContent = new SequenceInputStream(
                        new FileInputStream(new File(filePath)),
                        new ByteArrayInputStream(customEOF.getBytes()));
                InputStreamEntity requestEntity = new InputStreamEntity(entityContent);

                //FileEntity requestEntity = new FileEntity(new File(filePath), ContentType.create("text/plain", "UTF-8"));
                requestEntity.setChunked(true);
                request.setEntity(requestEntity);
                long inputStreamBuiltTime = System.nanoTime();
                CloseableHttpResponse response = httpClient.execute(request);

                try {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        long curTime = System.nanoTime();
                        System.out.printf("******  The No.%d round:  ******\n", loop);
                        System.out.printf("Build InputStream time : %f\n", (inputStreamBuiltTime - startTime) / 1000000000.00);
                        System.out.printf("Total send time : %f\n", (curTime - startTime) / 1000000000.00);
                        System.out.println();
                    }
                    HttpEntity responseEntity = response.getEntity();
                    //获得起始行
                    //System.out.println(response.getStatusLine().toString());
                    //获得首部
                /*Header[] hs = response.getAllHeaders();
                for (Header h : hs) {
                    System.out.println(h.getName() + ":\t" + h.getValue() + "\n");
                }*/
                    //获取实体，方法1：EntityUtils
                    //System.out.println("从服务器端获取的内容为：\n" + EntityUtils.toString(responseEntity));
                    EntityUtils.consume(responseEntity);
                } finally {
                    //entityContent.close();
                    response.close();
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        sendPost(args[0], args[1]);
        //sendPost("", "");
    }
}
