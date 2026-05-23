package com.bagro.integration;

import com.bagro.dto.external.ReniecResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ReniecClient {

    private final WebClient reniecWebClient;

    @Value("${reniec.dni-path:/dni}")
    private String dniPath;

    @Value("${reniec.timeout-ms:3000}")
    private long timeoutMs;

    public ReniecResponse consultarPorDni(String dni) {
        return reniecWebClient.get()
                .uri(uri -> uri
                        .path(dniPath)
                        .queryParam("numero", dni)
                        .build()
                )
                .retrieve()
                .bodyToMono(ReniecResponse.class)
                .block(Duration.ofMillis(timeoutMs));
    }
}