package com.example.saga.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Component
public class SignatureVerifier {

    private final boolean enabled;
    private final PublicKey publicKey;

    public SignatureVerifier(
            @Value("${SIGNATURE_PUBLIC_KEY_PATH}") Resource pemResource,
            @Value("${SIGNATURE_VERIFICATION_ENABLED:true}") boolean enabled
    ) throws Exception {
        this.enabled = enabled;
        String pem = new String(pemResource.getInputStream().readAllBytes());
        this.publicKey = loadPublicKeyFromPem(pem);
    }

    public boolean verifyRequest(String rawJson, String signatureBase64, String timestamp) {
        if (!enabled) return true;

        try {
            String signingString = timestamp + rawJson;

            byte[] sigBytes = Base64.getDecoder().decode(signatureBase64);

            Signature verifier = Signature.getInstance("SHA384withECDSA", "BC");
            verifier.initVerify(publicKey);
            verifier.update(signingString.getBytes(StandardCharsets.UTF_8));

            return verifier.verify(sigBytes);
        } catch (Exception e) {
            e.printStackTrace();
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
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return kf.generatePublic(spec);
    }
    private PublicKey loadPublicKeyFromFile(String path) throws Exception {
        String pem = Files.readString(Path.of(path));
        return loadPublicKeyFromPem(pem);
    }

}
