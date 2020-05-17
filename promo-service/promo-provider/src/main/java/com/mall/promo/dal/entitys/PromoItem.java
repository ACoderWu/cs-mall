package com.mall.promo.dal.entitys;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 20:57
 * @Version: 1.0
 */
@Table(name = "tb_promo_item")
@Data
@Builder
public class PromoItem {
    private Integer id;
    /**
     * 场次id主键
     *
     * @see com.mall.promo.dal.entitys.PromoSession
     */
    private Long psId;
    //商品id
    private Long itemId;
    //秒杀价格
    private BigDecimal seckillPrice;
    //秒杀库存
    private Integer stockNum;

}
