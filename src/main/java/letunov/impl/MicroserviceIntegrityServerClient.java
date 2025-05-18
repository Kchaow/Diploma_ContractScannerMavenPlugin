package letunov.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import letunov.exception.UnableToMakeRequestException;
import letunov.impl.data.MicroserviceContractsInfo;
import letunov.impl.data.VerificationInfo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

@Slf4j
public class MicroserviceIntegrityServerClient {
    private static final MediaType APPLICATION_JSON =  MediaType.parse("application/json");
    private static final String UPDATE_MICROSERVICE_GRAPH_URL = "/graph/microservice";
    private static final String VERIFY_MICROSERVICE_URL = "/change-graph";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();
    private final String baseURL;

    public MicroserviceIntegrityServerClient(String baseURL) {
        this.baseURL = baseURL;
    }

    public void updateMicroserviceGraph(MicroserviceContractsInfo microserviceContractsInfo) {
        var url = baseURL + UPDATE_MICROSERVICE_GRAPH_URL;
        var requestBody = RequestBody.create(getJson(microserviceContractsInfo), APPLICATION_JSON);
        var request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .build();
        sendRequest(request, url);
    }

    public void verifyMicroservice(MicroserviceContractsInfo microserviceContractsInfo, String graphId) {
        var url = baseURL + VERIFY_MICROSERVICE_URL + "/" + graphId;
        var requestBody = RequestBody.create(getJson(microserviceContractsInfo), APPLICATION_JSON);
        var request = new Request.Builder()
            .url(url)
            .put(requestBody)
            .build();
        sendRequest(request, url);
    }

    //    =========================================================================
    //    Implementation
    //    =========================================================================

    private void sendRequest(Request request, String url) {
        log.info("Sending request to microservice integrity server...");
        var call = client.newCall(request);
        try (var response = call.execute()) {
            if (response.code() != 200 && response.code() != 201) {
                throw new UnableToMakeRequestException(url, response.code());
            }
            log.debug("Request was sent successfully");
        } catch (IOException e) {
            throw new UnableToMakeRequestException(url, e);
        }
    }

    private String getJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
