package cn.intellimuyan.bardclient.model.msg;

import cn.intellimuyan.bardclient.model.Player;
import lombok.Data;

import java.util.List;

/**
 * @author hason
 * @version 19-1-31
 */
@Data
public class QueryPlayerMsg {
    private List<Player> players;
}
