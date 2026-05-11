const axios = require("axios");
const { sign } = require("./sign");

async function sendWebhook() {
    const body = JSON.stringify({
        transactionId: "tx-3",
        eventType: "COMMIT",
    });

    const timestamp = Date.now().toString();
    const signature = sign(timestamp, body);

    console.log("timestamp:", timestamp);
    console.log("body:", body);
    console.log("signature:", signature);

    try {
        const res = await axios.post(
            "http://localhost:6001/api/v1/webhook",
            body,
            {
                headers: {
                    "Content-Type": "application/json",
                    "X-Timestamp": timestamp,
                    "X-Signature": signature,
                },
                timeout: 2000,
            }
        );

        console.log("Response:", res.status);
    } catch (err) {
        console.log("Error:", err.response?.status || "NO_RESPONSE");
    }
}

sendWebhook();
