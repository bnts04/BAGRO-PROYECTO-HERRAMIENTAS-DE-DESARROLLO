package com.bagro.integration;

import com.bagro.dto.external.SunatRucResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SunatClient {

    private final WebClient sunatWebClient;

    @Value("${sunat.ruc-path:/ruc}")
    private String rucPath;

    @Value("${sunat.timeout-ms:3000}")
    private long timeoutMs;

    public SunatRucResponse consultarPorRuc(String ruc) {
        return sunatWebClient.get()
                .uri(uri -> uri
                        .path(rucPath)
                        .queryParam("numero", ruc)
                        .build()
                )
                .retrieve()
                .bodyToMono(SunatRucResponse.class)
                .block(Duration.ofMillis(timeoutMs));
    }
}