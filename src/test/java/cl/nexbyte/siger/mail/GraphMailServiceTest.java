package cl.nexbyte.siger.mail;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphMailServiceTest {
    @Test
    @SuppressWarnings("unchecked")
    void testPayloadOnlyTargetsConfiguredRecipient() {
        Map<String, Object> payload = GraphMailService.testPayload("siger@defensorianinez.cl");
        Map<String, Object> message = (Map<String, Object>) payload.get("message");
        List<Map<String, Object>> recipients = (List<Map<String, Object>>) message.get("toRecipients");
        Map<String, Object> address = (Map<String, Object>) recipients.getFirst().get("emailAddress");
        assertEquals(1, recipients.size());
        assertEquals("siger@defensorianinez.cl", address.get("address"));
        assertEquals(true, payload.get("saveToSentItems"));
        assertEquals("Text", ((Map<String, String>) message.get("body")).get("contentType"));
    }
}
