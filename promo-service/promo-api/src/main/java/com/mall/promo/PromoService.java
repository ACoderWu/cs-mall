package com.mall.promo;

import com.mall.promo.dto.CreatePromoOrderRequest;
import com.mall.promo.dto.CreatePromoOrderResponse;
import com.mall.promo.dto.PromoInfoRequest;
import com.mall.promo.dto.PromoInfoResponse;

import java.sql.SQLException;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 21:20
 * @Version: 1.0
 */
public interface PromoService {
    PromoInfoResponse queryPromoInfo(PromoInfoRequest request);

    CreatePromoOrderResponse createPromoOrder(CreatePromoOrderRequest request);
}
