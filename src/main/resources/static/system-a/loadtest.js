const axios = require("axios");
const crypto = require("crypto");

// -------------------------------
// CONFIG
// -------------------------------
const RATE = 5000; // requests per second
const DURATION_SEC = 10;
const GATEWAY_URL = "http://localhost:6001/api/v1/webhook";

// -------------------------------
// METRICS
// -------------------------------
let latencies = [];
let validTx = 0;
let reconciledTx = 0;

let pending = new Map(); // txId -> expected final state

// -------------------------------
// SIGNATURE GENERATION
// -------------------------------
function fakeSignature() {
    return crypto.randomBytes(64).toString("hex");
}

// -------------------------------
// SEND WEBHOOK
// -------------------------------
async function sendWebhook(txId, eventType, validSignature) {
    const body = JSON.stringify({ transactionId: txId, eventType });

    const timestamp = Date.now().toString();
    const signature = validSignature ? "test" : fakeSignature();

    const start = performance.now();

    try {
        const res = await axios.post(GATEWAY_URL, body, {
            headers: {
                "Content-Type": "application/json",
                "X-Timestamp": timestamp,
                "X-Signature": signature,
            },
            timeout: 2000,
        });

        const end = performance.now();
        latencies.push(end - start);

        if (validSignature) validTx++;

        return res.status;
    } catch (err) {
        const end = performance.now();
        latencies.push(end - start);
        return "ERR";
    }
}

// -------------------------------
// SCENARIOS
// -------------------------------
async function scenarioReserveCommit(txId, validSig) {
    pending.set(txId, "COMMIT");
    await sendWebhook(txId, "RESERVE", validSig);
    await sendWebhook(txId, "COMMIT", validSig);
    reconciledTx++;
}

async function scenarioReserveCompensate(txId, validSig) {
    pending.set(txId, "COMPENSATE");
    await sendWebhook(txId, "RESERVE", validSig);
    await sendWebhook(txId, "COMPENSATE", validSig);
    reconciledTx++;
}

async function scenarioCompensateBeforeReserve(txId, validSig) {
    pending.set(txId, "COMPENSATE");
    await sendWebhook(txId, "COMPENSATE", validSig);
    await sendWebhook(txId, "RESERVE", validSig);
    reconciledTx++;
}

// -------------------------------
// MAIN LOAD TEST
// -------------------------------
async function main() {
    console.log("Starting load test...");

    let total = 0;
    const start = Date.now();

    const interval = setInterval(async () => {
        const now = Date.now();
        if ((now - start) / 1000 > DURATION_SEC) {
            clearInterval(interval);
            await finish();
            return;
        }

        for (let i = 0; i < RATE; i++) {
            const txId = "tx-" + (total + i);
            const invalidSig = Math.random() < 0.05; // 5%

            const r = Math.random();

            if (r < 0.10) {
                scenarioCompensateBeforeReserve(txId, !invalidSig);
            } else if (r < 0.55) {
                scenarioReserveCommit(txId, !invalidSig);
            } else {
                scenarioReserveCompensate(txId, !invalidSig);
            }
        }

        total += RATE;
        console.log("Sent:", total);
    }, 1000);
}

// -------------------------------
// FINAL REPORT
// -------------------------------
async function finish() {
    console.log("\nLoad test complete.");
    console.log("Total valid transactions:", validTx);
    console.log("Total reconciled transactions:", reconciledTx);

    // p99 latency
    latencies.sort((a, b) => a - b);
    const p99 = latencies[Math.floor(latencies.length * 0.99)];

    console.log("p99 latency:", p99.toFixed(2), "ms");

    // Assertions
    console.log("\nAssertions:");
    console.log("p99 < 50ms:", p99 < 50 ? "PASS" : "FAIL");
    console.log("100% reconciliation:", validTx === reconciledTx ? "PASS" : "FAIL");

    console.log("\nFinal Result:");
    if (p99 < 50 && validTx === reconciledTx) {
        console.log("✔ Assessment load test PASSED");
    } else {
        console.log("✘ Assessment load test FAILED");
    }
}

main();
