package com.melli.hub.domain.response.base;


import com.melli.hub.util.date.DateUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.ThreadContext;

import java.util.Date;

@ToString
@Setter
@Getter
public class BaseResponse<T> {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Boolean success;
    private T data;
    private ErrorDetail errorDetail;
    private String trackingId;
    private String doTime;
    private Long doTimestamp;

    public BaseResponse() {
    }

    public BaseResponse(boolean success) {
        this.success = success;
        this.trackingId = ThreadContext.get("uuid");
        Date date = new Date();
        this.doTime = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, date, DATE_FORMAT, false);
        this.doTimestamp = date.getTime();
    }

    public BaseResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
        this.trackingId = ThreadContext.get("uuid");
        Date date = new Date();
        this.doTime = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, date, DATE_FORMAT, false);
        this.doTimestamp = date.getTime();
    }

    public BaseResponse(boolean success, ErrorDetail error) {
        this.success = success;
        this.errorDetail = error;
        this.trackingId = ThreadContext.get("uuid");
        Date date = new Date();
        this.doTime = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, date, DATE_FORMAT, false);
        this.doTimestamp = date.getTime();
    }
}
