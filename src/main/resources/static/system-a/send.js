import fs from "fs";
import axios from "axios";
import crypto from "crypto";

// Path to private key relative to this file
const PRIVATE_KEY_PATH = "../../keys/private.pem";

// Load private key
const privateKeyPem = fs.readFileSync(new URL(PRIVATE_KEY_PATH, import.meta.url), "utf8");
const privateKey = crypto.createPrivateKey({
    key: privateKeyPem,
    format: "pem",
    type: "pkcs8"
});

// Sign payload
function signPayload(rawJson) {
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

// Send webhook
async function sendWebhook(txId, status) {
    const rawJson = JSON.stringify({ transactionId: txId, status });

    const { timestamp, signature } = signPayload(rawJson);

    const headers = {
        "Content-Type": "application/json",
        "X-Timestamp": timestamp,
        "X-Signature": signature
    };

    console.log("Sending webhook...");
    console.log("Timestamp:", timestamp);
    console.log("Signature:", signature);

    try {
        const res = await axios.post("http://localhost:6001/api/v1/webhook", rawJson, { headers });
        console.log("Gateway response:", res.status, res.statusText);
    } catch (err) {
        console.error("Error:", err.response?.status, err.response?.data);
    }
}

// CLI usage
const [,, txId, status] = process.argv;

if (!txId || !status) {
    console.log("Usage: node send.js <transactionId> <status>");
    process.exit(1);
}

sendWebhook(txId, status);
