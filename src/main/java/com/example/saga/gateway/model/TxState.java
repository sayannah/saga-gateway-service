package com.example.saga.gateway.model;

public enum TxState {
    NONE,
    RESERVED,
    COMMITTED,
    COMPENSATED
}
