package com.mall.shopping.dto;/**
 * Created by ciggar on 2019/8/1.
 */

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.constants.ShoppingRetCode;
import lombok.*;

import java.util.List;

/**
 *  ciggar
 * create-date: 2019/8/1-下午10:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClearCartItemRequest extends AbstractRequest{

    private Long userId;
    private List<Long> productIds;
    @Override
    public void requestCheck() {
        if(userId==null){
            throw new ValidateException(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getCode(),ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
    }
}
