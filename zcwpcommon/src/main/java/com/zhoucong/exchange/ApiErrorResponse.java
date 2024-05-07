package com.zhoucong.exchange;

public record ApiErrorResponse(ApiError error, String data, String message) {

}
