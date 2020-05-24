package com.mall.order.converter;

import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.dto.CartProductDto;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.shopping.dal.entitys.Item;
import com.mall.shopping.dal.persistence.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;

/**
 * @Author: Li Qing
 * @Create: 2020/5/18 17:47
 * @Version: 1.0
 */
@Component
public class PromoOrderConverter {
    @Autowired
    ItemMapper itemMapper;

    /**
     * 转换CreateSeckillOrderRequest 为 CreateOrderContext
     *
     * @param request
     * @return
     */
    public CreateOrderContext toCreateOrderContext(CreateSeckillOrderRequest request) {
        CreateOrderContext orderContext = new CreateOrderContext();
        ArrayList<Long> productIds = new ArrayList<>();
        ArrayList<CartProductDto> cartProductDtos = new ArrayList<>();
        cartProductDtos.add(queryCartProductDtoByproductId(request.getProductId()));
        productIds.add(request.getProductId());
        orderContext.setBuyProductIds(productIds);
        orderContext.setUserId(request.getUserId());
        orderContext.setUserName(request.getUsername());
        orderContext.setBuyerNickName(request.getUsername());
        orderContext.setOrderTotal(request.getPrice());
        orderContext.setCartProductDtoList(cartProductDtos);
        return orderContext;
    }

    private CartProductDto queryCartProductDtoByproductId(Long productId) {
        Example example = new Example(Item.class);
        Item item = itemMapper.selectByPrimaryKey(productId);
        CartProductDto productDto = new CartProductDto();
        productDto.setProductId(productId);
        productDto.setProductNum((long) 1);
        productDto.setProductImg(item.getImage());
        productDto.setLimitNum(item.getLimitNum().longValue());
        productDto.setProductName(item.getTitle());
        productDto.setSalePrice(item.getPrice());
        return productDto;
    }


}
