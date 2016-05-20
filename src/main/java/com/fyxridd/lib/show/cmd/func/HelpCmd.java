package com.fyxridd.lib.show.cmd.func;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.core.api.fancymessage.FancyMessagePart;
import com.fyxridd.lib.core.config.ConfigManager;
import com.fyxridd.lib.func.api.func.Func;
import com.fyxridd.lib.func.api.func.FuncType;
import com.fyxridd.lib.func.api.func.Default;
import com.fyxridd.lib.show.cmd.ShowPlugin;
import com.fyxridd.lib.show.cmd.config.CmdConfig;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        
        List<FancyMessage> result = new ArrayList<>();
        //组可用变量
        Map<String, Object> groupParams = new HashMap<>();
        {
            groupParams.put("page", page);
            groupParams.put("maxPage", maxPage);
        }
        
        for (int line:config.getHelpGroupHeaders()) {
            FancyMessage msg = get(player, line);
            for (FancyMessagePart mp: msg.getMessageParts().values()) {
            }
            MessageApi.
            result.add();
        }
    }

    /**
     * 查看指定命令组内的所有命令功能
     */
    @Func("cmd")
    public void seeCmd(CommandSender sender, String group, @Default("1") int page) {

    }
    
    private FancyMessage get(String player, int id, Object... args) {
        return config.getLang().get(player, id, args);
    }
}
