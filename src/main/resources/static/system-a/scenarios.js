const { sendWebhook } = require("./send");

async function scenarioReserveCommit(txId, validSig) {
    await sendWebhook(txId, "RESERVE", validSig);
    await sendWebhook(txId, "COMMIT", validSig);
}

async function scenarioReserveCompensate(txId, validSig) {
    await sendWebhook(txId, "RESERVE", validSig);
    await sendWebhook(txId, "COMPENSATE", validSig);
}

async function scenarioCompensateBeforeReserve(txId, validSig) {
    await sendWebhook(txId, "COMPENSATE", validSig);
    await new Promise(r => setTimeout(r, 30));
    await sendWebhook(txId, "RESERVE", validSig);
}

module.exports = {
    scenarioReserveCommit,
    scenarioReserveCompensate,
    scenarioCompensateBeforeReserve,
};
