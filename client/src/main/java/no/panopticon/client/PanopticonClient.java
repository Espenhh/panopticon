package no.panopticon.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.panopticon.client.model.Status;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class PanopticonClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String baseUri;
    private CloseableHttpClient client;

    public PanopticonClient(String baseUri) {
        this.baseUri = baseUri;
        client = HttpClientBuilder.create().build();
    }

    public void update(Status status) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(status);
            String uri = baseUri + "/external/status";

            LOG.info("Updating status: " + uri);
            LOG.info("...with JSON: " + json);

            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(json.getBytes()));

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            CloseableHttpResponse response = client.execute(httpPost);

            LOG.info("Response: " + response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
