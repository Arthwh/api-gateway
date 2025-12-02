package com.sistemaEventos.api_gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    private final AuthenticationFilter filter;

    @Autowired
    public GatewayConfig(AuthenticationFilter filter) {
        this.filter = filter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // --- ROTAS DE NEGÓCIO ---
                .route("servico-usuarios", r -> r.path("/users/**", "/auth/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://servico-usuarios"))
                .route("servico-eventos", r -> r.path("/events/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://servico-eventos"))
                .route("servico-inscricoes", r -> r.path("/registrations/**", "/api/certificates/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://servico-inscricoes"))
                .route("servico-notificacoes", r -> r.path("/api/notifications/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://servico-notificacoes"))

                // --- ROTAS DO SWAGGER (Abertas) ---

                .route("docs-usuarios", r -> r.path("/v3/api-docs/servico-usuarios")
                        .filters(f -> f.rewritePath("/v3/api-docs/servico-usuarios", "/v3/api-docs"))
                        .uri("lb://servico-usuarios"))

                .route("docs-eventos", r -> r.path("/v3/api-docs/servico-eventos")
                        .filters(f -> f.rewritePath("/v3/api-docs/servico-eventos", "/v3/api-docs"))
                        .uri("lb://servico-eventos"))

                .route("docs-inscricoes", r -> r.path("/v3/api-docs/servico-inscricoes")
                        .filters(f -> f.rewritePath("/v3/api-docs/servico-inscricoes", "/v3/api-docs"))
                        .uri("lb://servico-inscricoes"))

                .route("docs-notificacoes", r -> r.path("/v3/api-docs/servico-notificacoes")
                        // AQUI ESTÁ O TRUQUE: O Java chama /v3/api-docs..., mas o Gateway traduz para /openapi.json
                        .filters(f -> f.rewritePath("/v3/api-docs/servico-notificacoes", "/openapi.json"))
                        .uri("lb://servico-notificacoes"))

                .build();
    }
}
