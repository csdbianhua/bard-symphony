package cn.intellimuyan.bardsymphony.nettyserver.model.msg;


import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import lombok.Data;

import java.util.Collection;

/**
 * @author hason
 * @version 19-1-31
 */
@Data
public class QueryPlayerMsg {
    private Collection<Player> players;
}
