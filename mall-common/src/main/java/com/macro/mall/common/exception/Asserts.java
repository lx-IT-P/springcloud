package com.macro.mall.common.exception;

import com.macro.mall.common.api.IErrorCode;
import com.macro.mall.common.enums.ExceptionEnum;

/**
 * 断言处理类，用于抛出各种API异常
 * Created by macro on 2020/2/27.
 */
public class Asserts {
    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }

    public static void fail(ExceptionEnum exceptionEnum) {
        throw new ApiException(exceptionEnum);
    }
}
