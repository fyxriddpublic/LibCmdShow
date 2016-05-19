package com.fyxridd.lib.show.cmd.context;

import java.util.List;
import java.util.Map;

/**
 * 命令上下文
 */
public class CmdContext {
    //命名别名列表(小写)(可为空列表不为null)
    private List<String> aliases;
    
    //功能列表(可为空列表不为null)
    //检测顺序(从1开始递增) 功能上下文
    private Map<Integer, FuncContext> funcs;

    public CmdContext(List<String> aliases, Map<Integer, FuncContext> funcs) {
        super();
        this.aliases = aliases;
        this.funcs = funcs;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public Map<Integer, FuncContext> getFuncs() {
        return funcs;
    }
}
