package com.mall.order.dto;
/**
 * Created by ciggar on 2019/7/30.
 */

import com.mall.commons.result.AbstractResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * ciggar
 * create-date: 2019/7/30-上午9:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpecialOrderResponse extends AbstractResponse {
    private static final long serialVersionUID = -8088848512700450581L;
    private String userName;
    private Double orderTotal;
    private Long   userId;
    private String tel;
    private String StreetName;
    List<OrderItemDto> goodsList;
}
