package com.macro.mall.common.exception;

import com.macro.mall.common.enums.ExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * @Author: liuxiang
 * @Date: 2020/4/29
 * @Description:
 */
public abstract class Assert {
    public Assert(){

    }
    public static void notNull(@Nullable Object Object, ExceptionEnum exceptionEnum){
        if (Object == null) {
            throw new ApiException(exceptionEnum);
        }
     }

    public static void notEmpty(int count,ExceptionEnum exceptionEnum){
        if (count == 0) {
            throw new ApiException(exceptionEnum);
        }
    }
}
