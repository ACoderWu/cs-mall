package com.mall.promo.dal.entitys;

import com.sun.javafx.beans.IDProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
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
@AllArgsConstructor
@NoArgsConstructor
public class PromoItem {
    @Id
    private Integer id;
    /**
     * 场次id主键
     *
     * @see com.mall.promo.dal.entitys.PromoSession
     */
    @Column(name = "ps_id")
    private Long psId;
    //商品id
    @Column(name = "item_id")
    private Long itemId;
    //秒杀价格
    @Column(name = "seckill_price")
    private BigDecimal seckillPrice;
    //秒杀库存
    @Column(name = "item_stock")
    private Integer stockNum;
}
