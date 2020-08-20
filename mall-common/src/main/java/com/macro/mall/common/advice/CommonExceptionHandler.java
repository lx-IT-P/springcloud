package com.macro.mall.common.advice;


import com.macro.mall.common.exception.ApiException;
import com.macro.mall.common.vo.ExceptionResult;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @Author: cuzz
 * @Date: 2018/10/31 19:06
 * @Description: 拦截异常
 */
@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionResult> handleException(ApiException e) {
        val em  = e.getExceptionEnum();
        val mes = e.getMessage();
        return (em != null) ? ResponseEntity.status(em.getCode()).body(new ExceptionResult(em))
                : ResponseEntity.status(404).body(new ExceptionResult(mes)) ;
    }
}
