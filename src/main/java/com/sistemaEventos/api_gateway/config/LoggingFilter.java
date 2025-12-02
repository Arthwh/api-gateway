package com.sistemaEventos.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    private static final Logger auditLog = LoggerFactory.getLogger("audit");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();
        // O "wrapper" .then(Mono.fromRunnable(...)) é a forma "Reativa" de dizer:
        // "Execute o resto da cadeia de filtros (chain.filter) e,
        //  DEPOIS que tudo terminar, execute este código."
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {

            long duration = System.currentTimeMillis() - startTime;

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            String ip = "N/A";
            if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
                ip = request.getRemoteAddress().getAddress().getHostAddress();
            }
            String method = request.getMethod().name();
            String path = request.getPath().toString();

            HttpStatusCode status = response.getStatusCode();
            int statusCode = (status != null) ? status.value() : 0;

            String userId = request.getHeaders().getFirst("X-User-ID");
            String userRoles = request.getHeaders().getFirst("X-User-Roles");

            String logMessage = String.format(
                    "[AUDIT LOG] IP=[%s] UserID=[%s] Roles=[%s] | %s %s | Status=[%d] Duration=[%dms]",
                    ip,
                    (userId != null ? userId : "ANONYMOUS"),
                    (userRoles != null ? userRoles : "N/A"),
                    method,
                    path,
                    statusCode,
                    duration
            );

            System.out.println(logMessage);
            auditLog.info(logMessage);
        }));
    }

    @Override
    public int getOrder() {
        // Ao usar LOWEST_PRECEDENCE (Prioridade Mais Baixa), garantimos
        // que o AuthenticationFilter (que tem prioridade mais alta)
        // já tenha executado e injetado os headers X-Usuario-ID.
        return Ordered.LOWEST_PRECEDENCE;
    }
}