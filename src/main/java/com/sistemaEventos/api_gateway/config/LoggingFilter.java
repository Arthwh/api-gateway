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

    // Vamos usar um logger dedicado chamado "audit"
    // (Isto permite-nos, no futuro, separar este log num ficheiro audit.log)
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

            // 1. Pega os dados básicos (IP, Método, Rota)
            String ip = "N/A";
            if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
                ip = request.getRemoteAddress().getAddress().getHostAddress();
            }
            String method = request.getMethod().name();
            String path = request.getPath().toString();

            // 2. Pega o status da resposta (ex: 200, 401, 500)
            HttpStatusCode status = response.getStatusCode();
            int statusCode = (status != null) ? status.value() : 0;

            // 3. Pega os headers que o AuthenticationFilter injetou
            String userId = request.getHeaders().getFirst("X-User-ID");
            String userRoles = request.getHeaders().getFirst("X-User-Roles");

            // 4. Formata e escreve o log
            // (Usamos "ANONYMOUS" se o ID do usuário for nulo,
            //  o que acontece em rotas públicas como /auth/login)
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
        // Esta é a parte mais importante.
        // Queremos que este filtro execute o mais TARDE possível.
        // Ao usar LOWEST_PRECEDENCE (Prioridade Mais Baixa), garantimos
        // que o AuthenticationFilter (que tem prioridade mais alta)
        // já tenha executado e injetado os headers X-Usuario-ID.
        return Ordered.LOWEST_PRECEDENCE;
    }
}