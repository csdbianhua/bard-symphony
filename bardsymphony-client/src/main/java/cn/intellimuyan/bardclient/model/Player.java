package cn.intellimuyan.bardclient.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Player {
    public static final Player EMPTY = new Player();
    private String id;
    private String name;

    public String toString() {
        if (id == null) {
            return "";
        }
        return id + "," + name;
    }
}
