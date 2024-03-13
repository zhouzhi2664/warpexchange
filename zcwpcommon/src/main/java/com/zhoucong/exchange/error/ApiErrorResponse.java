package com.zhoucong.exchange.error;

public record ApiErrorResponse(ApiError error, String data, String message) {

}
