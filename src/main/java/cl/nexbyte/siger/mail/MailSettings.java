package cl.nexbyte.siger.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "siger.mail")
public record MailSettings(
        @NotBlank @Pattern(regexp = "[a-fA-F0-9-]{36}") String tenantId,
        @NotBlank @Pattern(regexp = "[a-fA-F0-9-]{36}") String clientId,
        @NotBlank String clientSecret,
        @NotBlank @Email String sender,
        @NotBlank @Email String recipient,
        @NotBlank String localTestPassword
) {}
