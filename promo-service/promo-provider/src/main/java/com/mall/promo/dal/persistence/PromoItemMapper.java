package com.mall.promo.dal.persistence;


import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.promo.dal.entitys.PromoItem;
import com.mall.promo.dto.PromoItemInfoDto;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;


public interface PromoItemMapper extends TkMapper<PromoItem> {
    /**
     * 联合promo_item以及item两张表查询效率更高
     *
     * @param psId
     * @return
     * @throws SQLException
     */
    List<PromoItemInfoDto> selectPromoItemInfoDtoByPsId(@Param("psId") Long psId) throws SQLException;

    int updateStock(@Param("itemId") Long productId, @Param("psId") Long psId, @Param("num") Integer num);
}