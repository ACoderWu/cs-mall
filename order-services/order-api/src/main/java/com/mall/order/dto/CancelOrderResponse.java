package com.mall.order.dto;/**
 * Created by ciggar on 2019/7/30.
 */

import com.mall.commons.result.AbstractResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *  ciggar
 * create-date: 2019/7/30-上午9:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelOrderResponse extends AbstractResponse{
    private static final long serialVersionUID = 2745172244591729848L;
    private String result;
}
