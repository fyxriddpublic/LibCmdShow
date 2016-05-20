package com.fyxridd.lib.show.cmd.context;

import java.util.Map;

/**
 * 命令组上下文
 */
public class GroupContext {
    private String group;

    //组名(可为null表示无组名)
    private String name;
    
    //描述(可为null表示无描述)
    private String desc;
    
    //命令列表(可为空列表不为null)
    //命令名(小写) 命令上下文
    private Map<String, CmdContext> cmds;

    public GroupContext(String group, String name, String desc, Map<String, CmdContext> cmds) {
        super();
        this.group = group;
        this.name = name;
        this.desc = desc;
        this.cmds = cmds;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Map<String, CmdContext> getCmds() {
        return cmds;
    }
}
