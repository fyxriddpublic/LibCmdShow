package com.fyxridd.lib.show.cmd.func;

import com.fyxridd.lib.func.api.func.Func;
import com.fyxridd.lib.func.api.func.FuncType;
import com.fyxridd.lib.func.api.func.Default;
import org.bukkit.command.CommandSender;

/**
 * 命令帮助
 */
@FuncType("cmd")
public class HelpCmd {
    /**
     * 查看所有命令组
     */
    @Func("group")
    public void seeGroup(CommandSender sender, @Default("1") int page) {

    }

    /**
     * 查看指定命令组内的所有命令功能
     */
    @Func("cmd")
    public void seeCmd(CommandSender sender, String group, @Default("1") int page) {

    }
}
