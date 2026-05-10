import express from "express";

const app = express();
app.use(express.json());

app.post("/api/v1/ack", (req, res) => {
    console.log("ACK RECEIVED:", req.body);
    res.sendStatus(200);
});

app.listen(8081, () => console.log("System A ACK server running on port 8081"));
