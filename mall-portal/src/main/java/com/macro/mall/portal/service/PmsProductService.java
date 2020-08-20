package com.macro.mall.portal.service;

import com.macro.mall.model.PmsProduct;

import java.util.List;

/**
 * 商品管理Service
 * Created by macro on 2018/4/26.
 */
public interface PmsProductService {

    /**
     * 根据商品id查询
     */
    PmsProduct list(Long id,String ip);
}
