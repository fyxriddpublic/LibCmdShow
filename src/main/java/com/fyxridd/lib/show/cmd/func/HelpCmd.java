package com.fyxridd.lib.show.cmd.func;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.core.config.ConfigManager;
import com.fyxridd.lib.func.api.func.Default;
import com.fyxridd.lib.func.api.func.Func;
import com.fyxridd.lib.func.api.func.FuncType;
import com.fyxridd.lib.show.cmd.ShowPlugin;
import com.fyxridd.lib.show.cmd.config.CmdConfig;
import com.fyxridd.lib.show.cmd.context.FuncContext;
import com.fyxridd.lib.show.cmd.context.GroupContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 命令帮助
 */
@FuncType("cmd")
public class HelpCmd {
    private CmdConfig config;
    
    public HelpCmd() {
        //添加配置监听
        ConfigApi.addListener(ShowPlugin.instance.pn, CmdConfig.class, new ConfigManager.Setter<CmdConfig>() {
            @Override
            public void set(CmdConfig value) {
                config = value;
            }
        });
    }
    
    /**
     * 查看所有命令组
     */
    @Func("group")
    public void seeGroup(CommandSender sender, @Default("1") int page) {
        Player p;
        if (sender instanceof Player) p = (Player) sender;
        else p = null;
        String player = p != null?p.getName():null;
        
        int maxPage = UtilApi.getMaxPage(config.getGroups().size(), config.getHelpGroupPageSize());
        if (maxPage < 1) maxPage = 1;
        //页面不对
        if (page < 1 || page > maxPage) {
            MessageApi.send(sender, get(player, 20, 1, maxPage), true);
            return;
        }

        //要显示的信息列表
        List<FancyMessage> result = new ArrayList<>();
        //组可用变量
        Map<String, Object> groupParams = new HashMap<>();
        {
            groupParams.put("page", page);
            groupParams.put("maxPage", maxPage);
        }

        //headers
        for (int line:config.getHelpGroupHeaders()) {
            FancyMessage msg = get(player, line);
            MessageApi.convert(msg, groupParams);
            result.add(msg);
        }

        //bodys
        for (Object o:UtilApi.getPage(config.getGroups().values(), 2, config.getHelpGroupPageSize(), page)) {
            GroupContext groupContext = (GroupContext) o;

            Map<String, Object> bodysParams = new HashMap<>();
            {
                bodysParams.putAll(groupParams);
                bodysParams.put("group", groupContext.getGroup());
                bodysParams.put("groupName", groupContext.getName());
                bodysParams.put("groupDesc", groupContext.getDesc());
            }
            for (int line:config.getHelpGroupBodys()) {
                FancyMessage msg = get(player, line);
                MessageApi.convert(msg, bodysParams);
                result.add(msg);
            }
        }

        //footers
        for (int line:config.getHelpGroupFooters()) {
            FancyMessage msg = get(player, line);
            MessageApi.convert(msg, groupParams);
            result.add(msg);
        }

        //显示信息
        MessageApi.send(sender, result, true);
    }

    /**
     * 查看指定命令组内的所有命令功能
     */
    @Func("cmd")
    public void seeCmd(CommandSender sender, String group, @Default("1") int page) {
        Player p;
        if (sender instanceof Player) p = (Player) sender;
        else p = null;
        String player = p != null?p.getName():null;

        //命令组不存在
        GroupContext groupContext = config.getGroups().get(group);
        if (groupContext == null) {
            MessageApi.send(sender, get(player, 30, group), true);
            return;
        }

        List<FuncContext> funcs = ShowPlugin.instance.getCmdManager().getGroupFuncs(group);
        if (funcs == null) funcs = new ArrayList<>();
        int maxPage = UtilApi.getMaxPage(funcs.size(), config.getHelpCmdPageSize());
        if (maxPage < 1) maxPage = 1;
        //页面不对
        if (page < 1 || page > maxPage) {
            MessageApi.send(sender, get(player, 20, 1, maxPage), true);
            return;
        }

        //要显示的信息列表
        List<FancyMessage> result = new ArrayList<>();
        //命令可用变量
        Map<String, Object> cmdParams = new HashMap<>();
        {
            cmdParams.put("page", page);
            cmdParams.put("maxPage", maxPage);
            cmdParams.put("group", group);
            cmdParams.put("groupName", groupContext.getName());
            cmdParams.put("groupDesc", groupContext.getDesc());
        }

        //headers
        for (int line:config.getHelpCmdHeaders()) {
            FancyMessage msg = get(player, line);
            MessageApi.convert(msg, cmdParams);
            result.add(msg);
        }

        //bodys
        Set<String> hasShownCmds_ = new HashSet<>();//已经显示过的cmd列表
        for (Object o:UtilApi.getPage(funcs, 0, config.getHelpCmdPageSize(), page)) {
            FuncContext funcContext = (FuncContext) o;

            Map<String, Object> cmdsParams = new HashMap<>();
            {
                cmdsParams.putAll(cmdParams);
                cmdsParams.put("cmd", funcContext.getCmd());
                cmdsParams.put("aliases", groupContext.getCmds().get(funcContext.getCmd()).getAliasesStr());
            }

            Map<String, Object> funcsParams = new HashMap<>();
            {
                funcsParams.putAll(cmdsParams);
                funcsParams.put("useage", funcContext.getUseage().replace("{cmd}", funcContext.getCmd()));
                funcsParams.put("desc", funcContext.getDesc());
            }

            //检测先显示命令行
            if (!hasShownCmds_.contains(funcContext.getCmd())) {
                hasShownCmds_.add(funcContext.getCmd());
                //显示
                for (int line:config.getHelpCmdBodysCmds()) {
                    FancyMessage msg = get(player, line);
                    MessageApi.convert(msg, cmdsParams);
                    result.add(msg);
                }
            }

            //再显示功能行
            for (int line:config.getHelpCmdBodysFuncs()) {
                FancyMessage msg = get(player, line);
                MessageApi.convert(msg, funcsParams);
                result.add(msg);
            }
        }

        //footers
        for (int line:config.getHelpCmdFooters()) {
            FancyMessage msg = get(player, line);
            MessageApi.convert(msg, cmdParams);
            result.add(msg);
        }

        //显示信息
        MessageApi.send(sender, result, true);
    }
    
    private FancyMessage get(String player, int id, Object... args) {
        return config.getLang().get(player, id, args);
    }
}
