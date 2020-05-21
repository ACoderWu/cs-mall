package com.mall.promo.dal.entitys;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 20:54
 * @Version: 1.0
 */
@Table(name = "tb_promo_session")
@Data
@ToString
public class PromoSession {
    @Id
    @Column(name = "id")
    private Long id;
    //场次id:上午场 1 下午场 2
    @Column(name = "session_id")
    private Integer sessionId;
    @Column(name = "start_time")
    private Date startTime;
    @Column(name = "end_time")
    private Date endTime;
    //场次时间
    @Column(name = "yyyymmdd")
    private String yyyyMMdd;
}
