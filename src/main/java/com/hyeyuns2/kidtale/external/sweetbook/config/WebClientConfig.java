package com.hyeyuns2.kidtale.external.sweetbook.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Slf4j
@Configuration
@EnableConfigurationProperties(SweetBookProperties.class)
public class WebClientConfig {

    @Bean
    public WebClient sweetBookWebClient(
            WebClient.Builder builder,
            SweetBookProperties properties
    ) {
        // Sandbox 환경은 중간 CA 인증서가 JVM truststore에 없어 SSLHandshakeException 발생.
        // InsecureTrustManagerFactory로 SSL 검증을 우회한다. (개발/샌드박스 전용)
        HttpClient httpClient = createInsecureHttpClient();

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.key())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private HttpClient createInsecureHttpClient() {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            log.warn("[WebClientConfig] SweetBook WebClient: SSL 검증을 우회합니다. (Sandbox 전용)");
            return HttpClient.create().secure(spec -> spec.sslContext(sslContext));
        } catch (SSLException e) {
            log.error("[WebClientConfig] SSL Context 생성 실패. 기본 설정으로 폴백합니다.", e);
            return HttpClient.create();
        }
    }
}
