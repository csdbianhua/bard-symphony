package cn.intellimuyan.bardclient.nettyclient.inbound;


import cn.intellimuyan.bardclient.model.BardCommand;
import cn.intellimuyan.bardclient.model.CmdType;
import cn.intellimuyan.bardclient.nettyclient.anno.CmdMapping;
import cn.intellimuyan.bardclient.nettyclient.anno.NettyController;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Service
@Order(1)
@ChannelHandler.Sharable
@Slf4j
public class CommandProcessor extends SimpleChannelInboundHandler<BardCommand> {

    private final Map<CmdType, Invoker> map = new HashMap<>();
    private final ApplicationContext ctx;

    public CommandProcessor(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Data
    private static class ParameterDesc {
        private static final List<Class<?>> NORMAL_TYPES =
                Arrays.asList(Number.class, String.class);
        private final Class<?> clz;
        private boolean treatAsPojo;

        ParameterDesc(Class<?> clz) {
            this.clz = clz;
            for (Class<?> normalType : NORMAL_TYPES) {
                if (clz.isAssignableFrom(normalType)) {
                    return;
                }
            }
            this.treatAsPojo = true;
        }
    }

    @AllArgsConstructor(staticName = "of")
    @Data
    private static class Invoker {
        private final Method method;
        private final Object obj;
        private final CmdType returnType;
        private final boolean hasResponse;
        private final Map<String, ParameterDesc> parameters = new LinkedHashMap<>();

        void putParameter(String name, Class<?> clz) {
            parameters.put(name, new ParameterDesc(clz));
        }

        @SneakyThrows
        Object invoke(Object... args) {
            return method.invoke(obj, args);
        }

    }

    @PostConstruct
    public void init() {
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        Map<String, Object> nettyControllers = ctx.getBeansWithAnnotation(NettyController.class);
        for (Object obj : nettyControllers.values()) {
            Method[] methods = obj.getClass().getDeclaredMethods();
            String simpleClassName = obj.getClass().getSimpleName();
            for (Method method : methods) {
                CmdMapping anno = method.getAnnotation(CmdMapping.class);
                if (anno == null) {
                    continue;
                }
                CmdType mapping = anno.mapping();
                CmdType returning = anno.returning();
                boolean hasResponse = returning != CmdType.NONE && method.getReturnType() != void.class;
                Invoker invoker = Invoker.of(method, obj, returning, hasResponse);

                String[] names = discoverer.getParameterNames(method);
                if (names != null) {
                    Parameter[] parameters = method.getParameters();
                    for (int i = 0; i < parameters.length; i++) {
                        invoker.putParameter(names[i], parameters[i].getType());
                    }
                }
                map.put(mapping, invoker);
                log.info("[命令映射] {} -> {}.{} (response:? {})", mapping, simpleClassName, method.getName(), hasResponse);
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BardCommand msg) throws Exception {
        CmdType cmd = msg.getCmd();
        Invoker invoker = map.get(cmd);
        if (invoker == null) {
            return;
        }
        Object[] args = generateArgs(invoker, msg.getPayload());
        Object result = invoker.invoke(args);
        if (invoker.hasResponse) {
            BardCommand response = new BardCommand();
            response.setPayload(result != null ? JSON.toJSONString(result) : null);
            response.setCmd(invoker.getReturnType());
            ctx.channel().writeAndFlush(response);
        }
    }

    private Object[] generateArgs(Invoker invoker, String payload) {
        Map<String, ParameterDesc> parameters = invoker.getParameters();
        Object[] args = new Object[parameters.size()];
        int index = 0;
        JSONObject json = null;
        for (Map.Entry<String, ParameterDesc> entry : parameters.entrySet()) {
            String name = entry.getKey();
            ParameterDesc desc = entry.getValue();
            Class<?> clz = desc.getClz();
            if (desc.isTreatAsPojo()) {
                args[index] = JSON.parseObject(payload, clz);
            } else {
                if (json == null) {
                    json = JSON.parseObject(payload);
                    if (json == null) {
                        json = new JSONObject(0);
                    }
                }
                args[index] = json.get(name);
            }
            index++;
        }
        return args;
    }


}
