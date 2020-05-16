package com.mall.promo.dal.entitys;

import lombok.Data;

import javax.persistence.Table;
import java.util.Date;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 20:54
 * @Version: 1.0
 */
@Table(name = "tb_promo_session")
@Data
public class PromoSession {
    private Long id;
    //场次id:上午场 1 下午场 2
    private Integer sessionId;
    private Date startTime;
    private Date endTime;
    //场次时间
    private String yyyyMMdd;
}
