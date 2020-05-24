package com.mall.promo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: Li Qing
 * @Create: 2020/5/17 11:15
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromoItemInfoDto implements Serializable {
    private static final long serialVersionUID = 4132170058797515498L;
    private Integer id;
    private Integer inventory;
    private BigDecimal price;
    private BigDecimal seckillPrice;
    private String picUrl;
    private String productName;
}
