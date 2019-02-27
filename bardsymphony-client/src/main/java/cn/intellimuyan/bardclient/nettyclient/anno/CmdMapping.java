package cn.intellimuyan.bardclient.nettyclient.anno;

import cn.intellimuyan.bardclient.model.CmdType;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 命令映射
 *
 * @author hason
 * @version 19-1-29
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CmdMapping {
    /**
     * 映射处理类型
     */
    CmdType mapping();

    /**
     * 返回命令
     */
    CmdType returning() default CmdType.NONE;
}
