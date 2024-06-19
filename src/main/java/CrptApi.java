import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int requestLimit;
    private final AtomicInteger requestCount;
    private final ScheduledExecutorService scheduler;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.requestLimit = requestLimit;
        this.requestCount = new AtomicInteger(0);
        this.scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> requestCount.set(0), 0, 1, timeUnit);
    }

    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        synchronized (this) {
            while (requestCount.get() >= requestLimit) {
                this.wait();
            }
            requestCount.incrementAndGet();
        }

        try {
            String requestBody = createRequestBody(document, signature);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response: " + response.body());

        } finally {
            synchronized (this) {
                requestCount.decrementAndGet();
                this.notifyAll();
            }
        }
    }

    private String createRequestBody(Document document, String signature) throws IOException {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("signature", signature);
        rootNode.set("document", objectMapper.valueToTree(document));
        return objectMapper.writeValueAsString(rootNode);
    }

    public static class Document {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;

        public static class Description {
            public String participantInn;
        }

        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }

    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

        Document.Description description = new Document.Description();
        description.participantInn = "string";

        Document.Product product = new Document.Product();
        product.certificate_document = "string";
        product.certificate_document_date = "2020-01-23";
        product.certificate_document_number = "string";
        product.owner_inn = "string";
        product.producer_inn = "string";
        product.production_date = "2020-01-23";
        product.tnved_code = "string";
        product.uit_code = "string";
        product.uitu_code = "string";

        Document document = new Document();
        document.description = description;
        document.doc_id = "string";
        document.doc_status = "string";
        document.doc_type = "LP_INTRODUCE_GOODS";
        document.importRequest = true;
        document.owner_inn = "string";
        document.participant_inn = "string";
        document.producer_inn = "string";
        document.production_date = "2020-01-23";
        document.production_type = "string";
        document.products = new Document.Product[]{product};
        document.reg_date = "2020-01-23";
        document.reg_number = "string";

        String signature = "signature";

        try {
            api.createDocument(document, signature);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
