package com.mall.order.biz.handler;

import com.alibaba.fastjson.JSON;
import com.mall.commons.tool.exception.BizException;
import com.mall.order.biz.context.AbsTransHandlerContext;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.StockMapper;
import com.mall.order.dto.CartProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 扣减库存处理器
 * @Author： wz
 * @Date: 2019-09-16 00:03
 **/
@Component
@Slf4j
public class SubStockHandler extends AbstractTransHandler {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @Transactional
    public boolean handle(TransHandlerContext context) {
        CreateOrderContext createOrderContext = (CreateOrderContext) context;
        List<CartProductDto> dtoList = createOrderContext.getCartProductDtoList();
        List<Long> productIds = createOrderContext.getBuyProductIds();
        if (CollectionUtils.isEmpty(productIds)) {
            productIds = dtoList.stream().map(CartProductDto::getProductId).collect(Collectors.toList());
        }
        productIds.sort(Long::compareTo);
        //锁定库存
        List<Stock> stockList = stockMapper.findStocksForUpdate(productIds);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new BizException("库存未初始化");
        }
        if (stockList.size() != productIds.size()) {
            throw new BizException("部分商品库存未初始化");
        }
        //扣减库存
        for (CartProductDto productDto : dtoList) {
            Long productId = productDto.getProductId();
            Long num = productDto.getProductNum();
            Long limitNum = productDto.getLimitNum();
            Long productNum = num > limitNum ? limitNum : num;
            Stock stock = new Stock();
            stock.setItemId(productId);
            stock.setLockCount(productNum.intValue());
            stock.setStockCount(-productNum);
            try {
                stockMapper.updateStock(stock);
            } catch (SQLException e) {
                throw new BizException(OrderRetCode.DB_EXCEPTION.getMessage());
            }
        }
        return true;
    }
}