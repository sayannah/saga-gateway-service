import fs from "fs";
import crypto from "crypto";

const PRIVATE_KEY_PATH = "../../keys/private.pem";

// Load private key
const privateKeyPem = fs.readFileSync(new URL(PRIVATE_KEY_PATH, import.meta.url), "utf8");
const privateKey = crypto.createPrivateKey({
    key: privateKeyPem,
    format: "pem",
    type: "pkcs8"
});

/**
 * Signs a raw JSON string using:
 *   SHA384 + ECDSA(secp384r1) + DER encoding
 *   signingString = timestamp + rawJson
 */
export function signPayload(rawJson) {
    const timestamp = Math.floor(Date.now() / 1000).toString();
    const signingString = timestamp + rawJson;

    const sign = crypto.createSign("SHA384");
    sign.update(signingString);
    sign.end();

    const derSignature = sign.sign({
        key: privateKey,
        dsaEncoding: "der"
    });

    return {
        timestamp,
        signature: derSignature.toString("base64")
    };
}
