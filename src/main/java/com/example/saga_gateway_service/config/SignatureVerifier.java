package com.example.saga_gateway_service.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SignatureVerifier {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static boolean verify(byte[] message, String base64Signature, String base64PublicKey) {
        try {
            byte[] sigBytes = Base64.getDecoder().decode(base64Signature);
            byte[] pubBytes = Base64.getDecoder().decode(base64PublicKey);

            KeyFactory kf = KeyFactory.getInstance("EC", "BC");
            PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pubBytes));

            Signature signature = Signature.getInstance("SHA384withECDSA", "BC");
            signature.initVerify(publicKey);
            signature.update(message);

            return signature.verify(sigBytes);
        } catch (Exception e) {
            return false;
        }
    }
}

