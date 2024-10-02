package org.moin.moneytransfer.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "transfer_quotes")
public class TransferQuote {

    @Id
    private String quoteId;

    private int amount;

    private long fee;

    private double exchangeRate;

    private double receiveAmount;

    private ZonedDateTime expiredAt;

    private String targetCurrency; // 추가된 필드

    // Getter와 Setter
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

    public double getReceiveAmount() {
        return receiveAmount;
    }

    public void setReceiveAmount(double receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public ZonedDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(ZonedDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getTargetCurrency() { // 추가된 Getter
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) { // 추가된 Setter
        this.targetCurrency = targetCurrency;
    }
}
