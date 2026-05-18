package br.com.sisumoni.backend.service;
 
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import br.com.sisumoni.backend.domain.Usuario;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // Gerar token JWT

    public String gerarToken (Usuario usuario){
        Map<String , Object> claims = new HashMap<>();
        claims.put("perfil", usuario.getPerfil().name());
        claims.put("nome", usuario.getNome());

        return Jwts.builder()
                .claims(claims)
                .subject(usuario.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getChave())
                .compact();
    }

    //Validar token

    public boolean tokenValido(String token, UserDetails userDetails){
        final String email = extrairEmail(token);
        return (email.equals(userDetails.getUsername()) && !tokenExpirado(token));
    }

    //Extrair dados do Token

    public String extrairEmail(String token){
        return extrairClaims(token).getSubject();
    }

    public String extrairPerfil(String token){
        return (String) extrairClaims(token).get("Perfil");
    }

    public boolean tokenExpirado(String token){
        return extrairClaims(token).getExpiration().before(new Date());
    }

    public Claims extrairClaims(String token){
        return Jwts.parser()
                .verifyWith(getChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public SecretKey getChave(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
