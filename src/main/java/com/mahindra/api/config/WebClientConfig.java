package com.mahindra.api.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${countrystatecity.base-url}")
    private String cscBaseUrl;

    @Value("${countrystatecity.api-key}")
    private String cscApiKey;

    @Value("${countrystatecity.timeout-seconds:30}")
    private int cscTimeout;

    @Bean
    public WebClient countryStateCityWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, cscTimeout * 1000)
                .responseTimeout(Duration.ofSeconds(cscTimeout));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(cscBaseUrl)
                .defaultHeader("X-CSCAPI-KEY", cscApiKey)
                .build();
    }
}
