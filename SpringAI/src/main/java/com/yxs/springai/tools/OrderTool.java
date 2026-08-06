package com.yxs.springai.tools;

import com.yxs.springai.service.OrderManageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderTool {

    @Autowired
    private OrderManageService orderManageService;

    @Tool(name = "refund_order", description = "根据用户传入的订单信息发起退款")
    public String refund(String orderId, String reason) {
        orderManageService.refund(orderId, reason);
    return "Order " + orderId + " status is pending";
    }
}
