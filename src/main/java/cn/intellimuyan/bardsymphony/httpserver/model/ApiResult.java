package cn.intellimuyan.bardsymphony.httpserver.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author hason
 * @version 19-1-29
 */
@Builder
@Data
public class ApiResult {
    private int status;
    private String msg;
    private Object data;
}
