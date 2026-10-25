package cl.nexbyte.siger.mail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class LocalTestController {
    private final GraphMailService mailService;
    private final AtomicBoolean testAlreadyRequested = new AtomicBoolean(false);

    public LocalTestController(GraphMailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/_test/mail")
    public ResponseEntity<Map<String, Object>> testMail() {
        if (!testAlreadyRequested.compareAndSet(false, true)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("result", "ALREADY_EXECUTED", "detail", "Una prueba por arranque. Reinicie la aplicacion si esta autorizado."));
        }
        try {
            mailService.sendTestMessage();
            return ResponseEntity.accepted().body(Map.of(
                    "result", "ACCEPTED_BY_GRAPH",
                    "httpGraph", 202,
                    "delivered", false,
                    "detail", "Graph acepto el envio; confirme entrega en Exchange Online."
            ));
        } catch (GraphMailService.MailUpstreamException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "result", "UPSTREAM_REJECTED",
                    "stage", ex.stage(),
                    "upstreamHttpStatus", ex.status() == null ? "NO_RESPONSE" : ex.status().toString(),
                    "detail", "No se imprime la respuesta remota para evitar divulgar credenciales o mensajes."
            ));
        }
    }
}
