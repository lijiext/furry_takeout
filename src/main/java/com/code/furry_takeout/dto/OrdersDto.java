package com.code.furry_takeout.dto;

import com.code.furry_takeout.entity.OrderDetail;
import com.code.furry_takeout.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {
    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

}
