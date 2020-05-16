package com.mall.order.dto;/**
 * Created by ciggar on 2019/7/30.
 */

import com.mall.commons.result.AbstractResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ciggar
 * create-date: 2019/7/30-上午9:58
 */
@Data
@AllArgsConstructor
public class DeleteOrderResponse extends AbstractResponse {
    private String result;
}

