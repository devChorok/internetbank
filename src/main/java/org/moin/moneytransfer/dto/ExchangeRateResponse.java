package org.moin.moneytransfer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponse {

    private String code;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("currencyUnit")
    private double currencyUnit;

    @JsonProperty("basePrice")
    private double basePrice;

    // Getter와 Setter
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public double getCurrencyUnit() {
        return currencyUnit;
    }

    public void setCurrencyUnit(double currencyUnit) {
        this.currencyUnit = currencyUnit;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
}
