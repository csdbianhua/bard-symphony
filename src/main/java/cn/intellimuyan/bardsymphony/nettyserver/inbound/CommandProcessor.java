package cn.intellimuyan.bardsymphony.nettyserver.inbound;

import cn.intellimuyan.bardsymphony.nettyserver.framework.anno.CmdMapping;
import cn.intellimuyan.bardsymphony.nettyserver.framework.anno.NettyController;
import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommand;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import cn.intellimuyan.bardsymphony.service.PlayerManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Service
@Order(1)
@Slf4j
@ChannelHandler.Sharable
public class CommandProcessor extends ChannelInboundHandlerAdapter {

    private final Map<CmdType, Invoker> map = new HashMap<>();
    private final ApplicationContext ctx;
    private final PlayerManager clientManager;

    public CommandProcessor(ApplicationContext ctx,
                            PlayerManager clientManager) {
        this.ctx = ctx;
        this.clientManager = clientManager;
    }

    @Data
    private static class ParameterDesc {
        private static final List<Class<?>> NORMAL_TYPES =
                Arrays.asList(Number.class, String.class);
        private final Class<?> clz;
        private boolean isPlayer;
        private boolean treatAsPojo;

        ParameterDesc(Class<?> clz) {
            this.clz = clz;
            this.isPlayer = clz.isAssignableFrom(Player.class);
            if (!isPlayer) {
                for (Class<?> normalType : NORMAL_TYPES) {
                    if (clz.isAssignableFrom(normalType)) {
                        return;
                    }
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BardCommand datum = (BardCommand) msg;
        processDatum(ctx, datum);
    }

    private void processDatum(ChannelHandlerContext ctx, BardCommand datum) {
        CmdType cmd = datum.getCmd();
        Invoker invoker = map.get(cmd);
        if (invoker == null) {
            return;
        }
        String payload = datum.getPayload();

        Object[] args = generateArgs(ctx, invoker, payload);
        Object result = invoker.invoke(args);
        if (invoker.hasResponse) {
            BardCommand response = new BardCommand();
            response.setPayload(result != null ? JSON.toJSONString(result) : "{}");
            response.setCmd(invoker.getReturnType());
            ctx.channel().writeAndFlush(response);
        }
    }

    private Object[] generateArgs(ChannelHandlerContext ctx, Invoker invoker, String payload) {
        Map<String, ParameterDesc> parameters = invoker.getParameters();
        Object[] args = new Object[parameters.size()];
        int index = 0;
        if (StringUtils.isEmpty(payload)) {
            payload = "{}";
        }
        JSONObject json = null;
        for (Map.Entry<String, ParameterDesc> entry : parameters.entrySet()) {
            String name = entry.getKey();
            ParameterDesc desc = entry.getValue();
            Class<?> clz = desc.getClz();
            if (desc.isPlayer()) {
                args[index] = clientManager.getPlayer(ctx.channel());
            } else if (desc.isTreatAsPojo()) {
                args[index] = JSON.parseObject(payload, clz);
            } else {
                if (json == null) {
                    json = JSON.parseObject(payload);
                }
                args[index] = json.get(name);
            }
            index++;
        }
        return args;
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        BardCommand datum = new BardCommand();
        datum.setCmd(CmdType.LEAVE);
        processDatum(ctx, datum);
        ctx.fireChannelInactive();
    }
}
