import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final int requestLimit;
    private final long intervalInMillis;
    private final ScheduledExecutorService scheduler;
    private final ReentrantLock lock = new ReentrantLock();
    private int requestCounter = 0;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.intervalInMillis = timeUnit.toMillis(1);
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::resetRequestCounter, intervalInMillis, intervalInMillis, TimeUnit.MILLISECONDS);
    }

    public void createDocument(Object document, String signature) {
        lock.lock();
        try {
            if (requestCounter >= requestLimit) {
                throw new Exception("Превышен лимит запросов!");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = objectMapper.writeValueAsString(document);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(URL);
                StringEntity requestEntity = new StringEntity(jsonDocument, ContentType.APPLICATION_JSON);
                httpPost.setEntity(requestEntity);
                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    System.out.printf("Document created:%s \nwith signature:%s\n\n", document, signature);
                    requestCounter++;
                }
            } catch (HttpResponseException e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private void resetRequestCounter() {
        lock.lock();
        try {
            requestCounter = 0;
        } finally {
            lock.unlock();
        }
    }
}
