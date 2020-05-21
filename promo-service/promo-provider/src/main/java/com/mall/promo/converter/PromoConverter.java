package com.mall.promo.converter;


import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.promo.dal.entitys.PromoItem;
import com.mall.promo.dto.CreatePromoOrderRequest;
import com.mall.promo.dto.PromoItemInfoDto;
import com.mall.shopping.dal.entitys.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.ArrayList;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 17:30
 * @Version: 1.0
 */
@Mapper(componentModel = "spring")
public interface PromoConverter {


    @Mappings({
            @Mapping(source = "promoItem.itemId", target = "id"),
            @Mapping(source = "promoItem.stockNum", target = "inventory"),
            @Mapping(source = "promoItem.seckillPrice", target = "seckillPrice"),
            @Mapping(source = "item.image", target = "picUrl"),
            @Mapping(source = "item.price", target = "price"),
            @Mapping(source = "item.title", target = "productName")
    })
    PromoItemInfoDto promoItemAndItem2InfoRes(PromoItem promoItem, Item item);

    //会循环调用单个转换方法
    ArrayList<PromoItemInfoDto> promoItemAndItem2InfoRes(ArrayList<PromoItem> promoItemList,ArrayList<Item> itemList);

    CreateSeckillOrderRequest toSecKillRequest(CreatePromoOrderRequest request);
}
