package cn.intellimuyan.bardsymphony.nettyserver.model.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "say")
public class ReplayMsg {
    private String replay;
}
