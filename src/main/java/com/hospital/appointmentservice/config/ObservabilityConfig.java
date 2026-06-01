package com.hospital.appointmentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ObservabilityConfig {

    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder,
                                          @Value("${user-service.base-url}") String userServiceBaseUrl) {
        return builder
                .baseUrl(userServiceBaseUrl)
                .filter((request, next) -> {
                    String requestId = MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY);
                    if (requestId == null || request.headers().containsKey(RequestCorrelationFilter.CORRELATION_ID_HEADER)) {
                        return next.exchange(request);
                    }
                    ClientRequest correlatedRequest = ClientRequest.from(request)
                            .header(RequestCorrelationFilter.CORRELATION_ID_HEADER, requestId)
                            .build();
                    return next.exchange(correlatedRequest);
                })
                .build();
    }
}
