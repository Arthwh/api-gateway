package com.sistemaEventos.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration corsConfig = new CorsConfiguration();

        // ORIGENS PERMITIDAS
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));

        // MÉTODOS PERMITIDOS
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));

        // CABEÇALHOS PERMITIDOS
        corsConfig.setAllowedHeaders(List.of("*"));

        // CREDENCIAIS
        corsConfig.setAllowCredentials(true);

        // TEMPO MÁXIMO (cache da resposta OPTIONS)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        // Mapeia a configuração CORS para todas as rotas (/**)
        source.registerCorsConfiguration("/**", corsConfig);

        // Cria o filtro CORS que será o primeiro a ser aplicado
        return new CorsWebFilter(source);
    }
}
