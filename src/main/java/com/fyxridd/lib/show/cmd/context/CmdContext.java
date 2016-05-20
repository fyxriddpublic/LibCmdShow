package com.fyxridd.lib.show.cmd.context;

import java.util.List;
import java.util.Map;

/**
 * 命令上下文
 */
public class CmdContext {
    private String cmd;

    //命名别名列表(小写)(可为空列表不为null)
    private List<String> aliases;
    //格式'xx,xx...'
    //为了效率
    //与aliases同步
    private String aliasesStr;

    //功能列表(可为空列表不为null)
    //检测顺序(从1开始递增) 功能上下文
    private Map<Integer, FuncContext> funcs;

    public CmdContext(String cmd, List<String> aliases, Map<Integer, FuncContext> funcs) {
        super();
        this.cmd = cmd;
        this.aliases = aliases;
        this.funcs = funcs;

        aliasesStr = "";
        boolean first = true;
        for (String aliase:aliases) {
            if (first) first = false;
            else aliasesStr += ",";
            aliasesStr += aliase;
        }
    }

    public String getCmd() {
        return cmd;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getAliasesStr() {
        return aliasesStr;
    }

    public Map<Integer, FuncContext> getFuncs() {
        return funcs;
    }
}
