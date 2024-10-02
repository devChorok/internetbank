package org.moin.moneytransfer.dto;

public class TransferQuoteRequest {
    private int amount;
    private String targetCurrency;

    // Getter와 Setter
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
}
