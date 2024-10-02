package org.moin.moneytransfer.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class TransferHistoryResponse {
    private String transferId;
    private int amount;
    private String targetCurrency;
    private double receiveAmount;
    private long fee;
    private double exchangeRate;
    private ZonedDateTime transferredAt;

    // Getter와 Setter
}
