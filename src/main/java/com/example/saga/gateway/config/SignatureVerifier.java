package com.example.saga.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class SignatureVerifier {

    private final boolean enabled;
    private final PublicKey publicKey;

    public SignatureVerifier(
            @Value("${SIGNATURE_PUBLIC_KEY_PATH}") Resource pemResource,
            @Value("${SIGNATURE_VERIFICATION_ENABLED:true}") boolean enabled
    ) throws Exception {
        this.enabled = enabled;
        String pem = new String(pemResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        this.publicKey = loadPublicKeyFromPem(pem);
    }

    public boolean verifyRequest(String rawJson, String signatureHex, String timestamp) {
        if (!enabled) return true;

        try {
            String signingString = timestamp + rawJson;

            // SHA-384 → SHA-256 (32 bytes)
            MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
            byte[] h384 = sha384.digest(signingString.getBytes(StandardCharsets.UTF_8));

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] finalHash = sha256.digest(h384); // 32 bytes

            byte[] sigBytes = hexToBytes(signatureHex);

            Signature verifier = Signature.getInstance("NONEwithECDSA", "BC");
            verifier.initVerify(publicKey);
            verifier.update(finalHash);

            return verifier.verify(sigBytes);

        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }

    private PublicKey loadPublicKeyFromPem(String pem) throws Exception {
        String clean = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(clean);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }
}
