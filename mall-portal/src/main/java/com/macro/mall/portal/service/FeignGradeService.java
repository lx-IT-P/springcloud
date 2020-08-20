package com.macro.mall.portal.service;

import com.macro.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by macro on 2019/10/18.
 */
@FeignClient("mall-grade")
public interface FeignGradeService {

    @GetMapping("/word/putWord")
    CommonResult putWord(@RequestParam(value = "userId",required = true) String userId);
}
