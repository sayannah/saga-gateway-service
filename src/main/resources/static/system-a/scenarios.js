import { sendWebhook } from "./send.js";

export async function scenarioReserveCommit(txId, validSig) {
    await sendWebhook(txId, "RESERVE", validSig);
    await sendWebhook(txId, "COMMIT", validSig);
}

export async function scenarioReserveCompensate(txId, validSig) {
    await sendWebhook(txId, "RESERVE", validSig);
    await sendWebhook(txId, "COMPENSATE", validSig);
}

export async function scenarioCompensateBeforeReserve(txId, validSig) {
    await sendWebhook(txId, "COMPENSATE", validSig);
    await new Promise(r => setTimeout(r, 30));
    await sendWebhook(txId, "RESERVE", validSig);
}
