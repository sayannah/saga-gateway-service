package com.example.saga.gateway.config;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Component
public class SignatureVerifier {

    @Value("${signature.verification.enabled:true}")
    private boolean enabled;

    @Value("${signature.public-key-path}")
    private Resource publicKeyResource;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        if (!enabled) {
            System.out.println("Signature verification disabled");
            return;
        }

        try {
            String pem = new String(publicKeyResource.getInputStream().readAllBytes());
            String clean = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(clean);

            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            this.publicKey = kf.generatePublic(spec);

            System.out.println("Public key loaded successfully");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    public boolean verifyRequest(String rawJson, String signature, String timestamp) {
        if (!enabled) return true;

        // verification logic here
        return true;
    }
}
