package com.zhoucong.exchange.message;


import com.zhoucong.exchange.model.trade.OrderEntity;
import com.zhoucong.exchange.error.ApiError;
import com.zhoucong.exchange.error.ApiErrorResponse;

/**
 * API result message.
 */
public class ApiResultMessage extends AbstractMessage {
	
	public ApiErrorResponse error;

    public Object result;
    
    private static ApiErrorResponse CREATE_ORDER_FAILED = new ApiErrorResponse(ApiError.NO_ENOUGH_ASSET, null,
            "No enough available asset");

    private static ApiErrorResponse CANCEL_ORDER_FAILED = new ApiErrorResponse(ApiError.ORDER_NOT_FOUND, null,
            "Order not found..");
	
    public static ApiResultMessage orderSuccess(String refId, OrderEntity order, long ts) {
        ApiResultMessage msg = new ApiResultMessage();
        msg.result = order;
        msg.refId = refId;
        msg.createdAt = ts;
        return msg;
    }
	
    public static ApiResultMessage createOrderFailed(String refId, long ts) {
		ApiResultMessage msg = new ApiResultMessage();
		msg.error = CREATE_ORDER_FAILED;
        msg.refId = refId;
        msg.createdAt = ts;
		return msg;
	}
}
