package com.sistemaEventos.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtUtil {
    private final PublicKey publicKey;

    public JwtUtil(@Value("${jwt.public-key}") String publicKey) {
        // Construtor que carrega a Chave Pública do arquivo
        try {
            String publicKeyString = publicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyString);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.publicKey = kf.generatePublic(spec);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar chave pública", e);
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(this.publicKey) // Diz ao parser qual chave usar para VERIFICAR
                .build()
                .parseClaimsJws(token) // "Parseia" o token
                .getBody();
    }

    public boolean isInvalid(String token) {
        try {
            // Se ele conseguir "parsear", o token é válido (não expirado e assinado corretamente)
            this.getAllClaimsFromToken(token);
            return false;
        } catch (Exception e) {
            System.out.println(e);
            // Se der erro (expirado, assinatura errada), ele é inválido
            return true;
        }
    }
}
