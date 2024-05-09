package com.zhoucong.exchange.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.zhoucong.exchange.ApiError;
import com.zhoucong.exchange.ApiErrorResponse;
import com.zhoucong.exchange.ApiException;

import jakarta.servlet.http.HttpServletResponse;

public class AbstractApiController extends LoggerSupport{

	@ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ApiErrorResponse handleException(HttpServletResponse response, Exception ex) throws Exception {
		response.setContentType("application/json;charset=utf-8");
		ApiException apiEx = null;
		if (ex instanceof ApiException) {
            apiEx = (ApiException) ex;
        } else {
            apiEx = new ApiException(ApiError.INTERNAL_SERVER_ERROR, null, ex.getMessage());
        }
		return apiEx.error;
	}
}
