package com.melli.wallet.ipg.response;

public class SadedVerifyResponse {

    private String amount;
    private String refNUmber;
    private String orderId;
    private String status;
    private String traceNo;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRefNUmber() {
        return refNUmber;
    }

    public void setRefNUmber(String refNUmber) {
        this.refNUmber = refNUmber;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTraceNo() {
        return traceNo;
    }

    public void setTraceNo(String traceNo) {
        this.traceNo = traceNo;
    }
}
