package cn.intellimuyan.bardsymphony.httpserver.advice;

import cn.intellimuyan.bardsymphony.httpserver.model.ApiResult;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;


@RestControllerAdvice
@Slf4j
public class CommonAdvice {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult missingParameter(Exception e, HttpServletRequest r) {
        log.error("[意外]缺少参数,uri:{},parameterMap:{},queryString:{},msg:{}", r.getRequestURI(),
                JSON.toJSONString(r.getParameterMap()), r.getQueryString(),
                e.getMessage());
        return ApiResult.builder().status(400).msg(e.getMessage()).build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult error(Exception e, HttpServletRequest r) {
        log.error("[意外]未捕获的异常,uri:{},queryString:{},parameterMap:{}", r.getRequestURI(), r.getQueryString(),
                JSON.toJSONString(r.getParameterMap()), e);
        return ApiResult.builder().status(500).msg("系统异常").build();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity notFoundException() {
        return ResponseEntity.notFound().build();
    }


}
