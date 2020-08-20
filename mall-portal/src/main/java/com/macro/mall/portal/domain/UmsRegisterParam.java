package com.macro.mall.portal.domain;

import lombok.Data;

/**
 * @Author: liuxiang
 * @Date: 2020/4/26
 * @Description:
 */
@Data
public class UmsRegisterParam {
    private String username;
    private String password;
    private String authCode;
    private String telephone;
}
