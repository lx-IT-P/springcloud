package com.macro.mall.portal.controller;

import cn.hutool.core.lang.Dict;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.DictDO;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.portal.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: liuxiang
 * @Date: 2020/4/30
 * @Description:
 */
@Controller
@Api(tags = "DictController", description = "字典管理")
@RequestMapping("/dict")
public class DictController {
    @Autowired
    private DictService dictService;
    @ApiOperation("获取字典信息")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<DictDO>> getDictInformation() {
        List<DictDO> dict = dictService.list();
        return CommonResult.success(dict);
    }
}
