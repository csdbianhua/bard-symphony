package cn.intellimuyan.bardsymphony.nettyserver.framework.anno;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 表明被注解的类为netty控制器
 *
 * @author hason
 * @version 19-1-29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface NettyController {
}
