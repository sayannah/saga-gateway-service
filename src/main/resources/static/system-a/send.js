import axios from "axios";
import { signPayload } from "./sign.js";

async function sendWebhook(txId, eventType) {
    const rawJson = JSON.stringify({ transactionId: txId, eventType });
    const { timestamp, signature } = signPayload(rawJson);

    const headers = {
        "Content-Type": "application/json",
        "X-Timestamp": timestamp,
        "X-Signature": signature
    };

    console.log("Sending:", rawJson);
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
const [,, txId, eventType] = process.argv;

if (!txId || !eventType) {
    console.log("Usage: node send.js <transactionId> <RESERVE|COMMIT|COMPENSATE>");
    process.exit(1);
}

sendWebhook(txId, eventType);
