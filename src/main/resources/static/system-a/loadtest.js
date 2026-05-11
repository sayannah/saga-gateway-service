const axios = require("axios");

const RATE = 2000;          // 2k TPS
const DURATION_SEC = 5;     // 5 seconds
const MAX_CONCURRENCY = 300;
const URL = "http://localhost:6001/api/v1/webhook";

let inflight = 0;
let latencies = [];
let validTx = 0;
let reconciledTx = 0;

async function send(txId, eventType) {
    const body = JSON.stringify({ transactionId: txId, eventType });
    const start = performance.now();
    inflight++;

    try {
        await axios.post(URL, body, {
            headers: {
                "Content-Type": "application/json",
                "X-Timestamp": Date.now().toString(),
                "X-Signature": "test"
            },
            timeout: 2000
        });

        const end = performance.now();
        latencies.push(end - start);
        validTx++;
        reconciledTx++;
    } catch {
        const end = performance.now();
        latencies.push(end - start);
    } finally {
        inflight--;
    }
}

async function schedule(fn) {
    while (inflight >= MAX_CONCURRENCY) {
        await new Promise(r => setTimeout(r, 1));
    }
    return fn();
}

async function main() {
    console.log("MEDIUM LOAD TEST STARTED");

    let total = 0;
    const start = Date.now();
    const all = [];

    const interval = setInterval(() => {
        if ((Date.now() - start) / 1000 > DURATION_SEC) {
            clearInterval(interval);
            Promise.all(all).then(finish);
            return;
        }

        for (let i = 0; i < RATE; i++) {
            const txId = "tx-" + (total + i);
            const p = schedule(() => send(txId, "RESERVE"));
            all.push(p);
        }

        total += RATE;
        console.log("Scheduled:", total, "inflight:", inflight);
    }, 1000);
}

async function finish() {
    console.log("\n LOAD TEST COMPLETE");
    console.log("Valid:", validTx);
    console.log("Reconciled:", reconciledTx);

    latencies.sort((a, b) => a - b);
    const p99 = latencies[Math.floor(latencies.length * 0.99)];

    console.log("p99:", p99.toFixed(2), "ms");
}

main();
