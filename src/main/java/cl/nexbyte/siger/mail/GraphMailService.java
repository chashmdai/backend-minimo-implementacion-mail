package cl.nexbyte.siger.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GraphMailService {
    private final MailSettings settings;
    private final RestClient http;

    public GraphMailService(MailSettings settings, RestClient.Builder builder) {
        this.settings = settings;
        var jdkClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        var factory = new JdkClientHttpRequestFactory(jdkClient);
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.http = builder.requestFactory(factory).build();
    }

    /** Envia UNA prueba al destinatario configurado, nunca al recibido desde una peticion HTTP. */
    public void sendTestMessage() {
        String token = obtainToken();
        try {
            ResponseEntity<Void> response = http.post()
                    .uri("https://graph.microsoft.com/v1.0/users/{mailbox}/sendMail", settings.sender())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(testPayload(settings.recipient()))
                    .retrieve()
                    .toBodilessEntity();
            if (response.getStatusCode().value() != 202) {
                throw new MailUpstreamException("GRAPH", response.getStatusCode().value());
            }
        } catch (RestClientResponseException ex) {
            throw new MailUpstreamException("GRAPH", ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new MailUpstreamException("GRAPH", null);
        }
    }

    private String obtainToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", settings.clientId());
        form.add("client_secret", settings.clientSecret());
        form.add("grant_type", "client_credentials");
        form.add("scope", "https://graph.microsoft.com/.default");
        try {
            TokenResponse response = http.post()
                    .uri("https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token", settings.tenantId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new MailUpstreamException("TOKEN", null);
            }
            return response.accessToken();
        } catch (RestClientResponseException ex) {
            throw new MailUpstreamException("TOKEN", ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new MailUpstreamException("TOKEN", null);
        }
    }

    static Map<String, Object> testPayload(String recipient) {
        return Map.of(
                "message", Map.of(
                        "subject", "[SIGER] Prueba controlada de notificaciones - Nexbyte",
                        "body", Map.of(
                                "contentType", "Text",
                                "content", "Prueba tecnica de Microsoft Graph desde el backend local SIGER. No corresponde a una comunicacion real."
                        ),
                        "toRecipients", List.of(Map.of("emailAddress", Map.of("address", recipient)))
                ),
                "saveToSentItems", true
        );
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken) {}

    static final class MailUpstreamException extends RuntimeException {
        private final String stage;
        private final Integer status;

        MailUpstreamException(String stage, Integer status) {
            super("External service rejected request at stage " + stage + ", HTTP=" + status);
            this.stage = stage;
            this.status = status;
        }

        String stage() { return stage; }
        Integer status() { return status; }
    }
}
