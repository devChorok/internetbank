package org.moin.moneytransfer.dto;

import java.time.ZonedDateTime;

public class TransferResponse {
    private String transferId;
    private String quoteId;
    private int amount;
    private String targetCurrency;
    private double receiveAmount;
    private long fee;
    private double exchangeRate;
    private ZonedDateTime transferredAt;

    // Getter와 Setter
    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public double getReceiveAmount() {
        return receiveAmount;
    }

    public void setReceiveAmount(double receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public ZonedDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(ZonedDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }
}
