const crypto = require("crypto");
const secp256k1 = require("secp256k1");
const fs = require("fs");

const privateKey = fs.readFileSync(
    "C:/Users/sayan/saga-gateway-service/src/main/resources/keys/private.raw"
);

function sign(timestamp, body) {
    const data = timestamp + body;

    // SHA-384 → SHA-256 (32 bytes)
    const h384 = crypto.createHash("sha384").update(data).digest();
    const h256 = crypto.createHash("sha256").update(h384).digest(); // 32 bytes

    const { signature } = secp256k1.ecdsaSign(h256, privateKey);
    return Buffer.from(signature).toString("hex");
}

module.exports = { sign };
