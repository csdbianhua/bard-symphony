package cn.intellimuyan.bardclient.nettyclient;

import cn.intellimuyan.bardclient.model.CmdType;
import org.springframework.beans.factory.DisposableBean;

/**
 * 交响乐客户端
 *
 * @author hason
 * @version 19-1-31
 */
public interface ISymphonyClient extends DisposableBean {

    void connect();

    void close();

    boolean isOnline();

    void sendCmd(CmdType cmdType, Object payload);

    @Override
    default void destroy() throws Exception {
        close();
    }

}
