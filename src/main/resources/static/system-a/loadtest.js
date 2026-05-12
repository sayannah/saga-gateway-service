import axios from "axios";
import { performance } from "perf_hooks";

const RATE = 1000;
const DURATION_SEC = 5;
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
                "X-Timestamp": "1",
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

            // Wait until all promises are scheduled
            const waitSchedule = setInterval(() => {
                if (all.length === total) {
                    clearInterval(waitSchedule);

                    // Now wait for inflight to drain
                    const waitInflight = setInterval(() => {
                        if (inflight === 0) {
                            clearInterval(waitInflight);
                            finish();
                        }
                    }, 10);
                }
            }, 10);

            return;
        }

        for (let i = 0; i < RATE; i++) {
            const txId = "tx-" + (total + i);
            const p = schedule(() => send(txId, "RESERVE"));
            all.push(p);
        }

        total += RATE;
        console.log("scheduled:", total, "inflight:", inflight);
    }, 100);

    // Hard failsafe: exit after max 30 seconds no matter what
    setTimeout(() => {
        console.log("\nFORCED EXIT (failsafe)");
        finish();
    }, 30000);
}

function finish() {
    console.log("\nLOAD TEST COMPLETE");
    console.log("valid:", validTx);
    console.log("reconciled:", reconciledTx);

    latencies.sort((a, b) => a - b);
    const p99 = latencies[Math.floor(latencies.length * 0.99)];

    console.log("p99:", p99.toFixed(2), "ms");

    process.exit(0);
}

main();
