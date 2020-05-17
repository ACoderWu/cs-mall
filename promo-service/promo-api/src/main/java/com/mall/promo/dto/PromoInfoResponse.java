package com.mall.promo.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.*;

import java.util.List;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 17:30
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromoInfoResponse extends AbstractResponse {
    private static final long serialVersionUID = 5767225913239809439L;
    private Integer sessionId;
    private Long psId;
    private List<PromoItemInfoDto> productList;
}
