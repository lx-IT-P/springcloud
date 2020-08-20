package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.portal.domain.CartProduct;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.service.PmsProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author: liuxiang
 * @Date: 2020/4/27
 * @Description: 商品详情管理类
 */
@Controller
@Api(tags = "GoodsController", description = "商品管理")
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private PmsProductService pmsProductService;
    @ApiOperation("获取商品详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<PmsProduct> getCartProduct(@RequestParam Long id, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        PmsProduct pmsProducts = pmsProductService.list(id,remoteAddr);
        return CommonResult.success(pmsProducts);
    }
}
