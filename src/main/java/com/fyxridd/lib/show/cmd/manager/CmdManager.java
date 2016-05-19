package com.fyxridd.lib.show.cmd.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fyxridd.lib.core.api.CoreApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.EventExecutor;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.core.config.ConfigManager;
import com.fyxridd.lib.func.api.FuncApi;
import com.fyxridd.lib.show.cmd.ShowPlugin;
import com.fyxridd.lib.show.cmd.config.CmdConfig;
import com.fyxridd.lib.show.cmd.context.CmdContext;
import com.fyxridd.lib.show.cmd.context.FuncContext;
import com.fyxridd.lib.show.cmd.context.GroupContext;

public class CmdManager {
    public static final String CMD = "cmd";
    
    private CmdConfig config;

    //命令名 命令组
    private Map<String, GroupContext> cmdToGroups = new HashMap<>();
    //命令别名(命令名本身也在内) 命令名
    private Map<String, String> aliasToCmd = new HashMap<>();
    
    public CmdManager() {
        //添加配置监听
        ConfigApi.addListener(ShowPlugin.instance.pn, CmdConfig.class, new ConfigManager.Setter<CmdConfig>() {
            @Override
            public void set(CmdConfig value) {
                config = value;
                //更新缓存
                update();
            }
        });
        //注册功能类型
        FuncApi.registerTypeHook(CMD , config.getCmdFuncPrefix());
        //注册事件
        {
            //命令事件
            Bukkit.getPluginManager().registerEvent(PlayerCommandPreprocessEvent.class, ShowPlugin.instance, EventPriority.LOWEST, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event e) throws EventException {
                    PlayerCommandPreprocessEvent event = (PlayerCommandPreprocessEvent) e;
                    try {
                        //没有命令名,无法处理
                        if (event.getMessage().length() <= 1) return;
                        //解析
                        String[] args = event.getMessage().substring(1).split(" ");
                        String[] argsParam = new String[args.length-1];
                        if (args.length > 1) System.arraycopy(args, 1, argsParam, 0, args.length - 1);
                        //检测有无配置
                        String cmdName = getCmd(args[0]);
                        if (cmdName != null) {
                            GroupContext groupContext = getGroupContext(cmdName);
                            if (groupContext != null) {
                                CmdContext cmdContext = groupContext.getCmds().get(cmdName);
                                if (cmdContext != null) {
                                    int index = 1;
                                    FuncContext funcContext;
                                    while ((funcContext = cmdContext.getFuncs().get(index++)) != null) {
                                        //检测条件
                                        if (funcContext.getArgsLength() == -1 || funcContext.getArgsLength() == argsParam.length) {
                                            //检测权限
                                            if (funcContext.getPer() != null && !PerApi.has(event.getPlayer().getName(), funcContext.getPer())) {
                                                event.setCancelled(true);
                                                return;
                                            }
                                            //不转换
                                            if (funcContext.getConvertArgs() == null || funcContext.getConvertArgs().length == 0) return;
                                            //命令转换
                                            String result = "";
                                            boolean first = true;
                                            for (String s:funcContext.getConvertArgs()) {
                                                if (first) first = false;
                                                else result += " ";
                                                result += convert(event.getPlayer(), argsParam, s).trim();
                                            }
                                            //检测命令开头进行不同处理
                                            if (result.startsWith("/")) event.setMessage(result);
                                            else {
                                                event.setCancelled(true);
                                                //因为不是以'/'开头的,因此不会再次触发PlayerCommandPreprocessEvent事件,不用担心造成死循环
                                                event.getPlayer().chat(result);
                                            }
                                            return;
                                        }
                                    }
                                    return;
                                }
                            }
                        }
                        //无配置
                        if (config.isConvertDefaultDeny()) {//无配置的禁止
                            event.setCancelled(true);
                            MessageApi.send(event.getPlayer(), get(event.getPlayer().getName(), 5), true);
                        }
                    } catch (Exception e1) {
                        CoreApi.debug(UtilApi.convertException(e1));
                        MessageApi.send(event.getPlayer(), get(event.getPlayer().getName(), 15), true);
                    }
                }
            }, ShowPlugin.instance, true);
        }
    }
    
    /**
     * 由命令名获取命令组
     * @param cmd 命令名(命令别名不行)
     * @return 命令组,不存在返回null
     */
    public GroupContext getGroupContext(String cmd) {
        return cmdToGroups.get(cmd.toLowerCase());
    }
    
    /**
     * 由命令别名获取命令名
     * @param aliase 命令别名(如果传入的就是命令名,则原样返回)
     * @return (小写的)命令名,不存在返回null
     */
    public String getCmd(String aliase) {
        return aliasToCmd.get(aliase.toLowerCase());
    }

    /**
     * 变量转换,包括:
     * {name}
     * {x}
     * {x,}
     * {,y}
     * {x,y}
     * {,}
     * @param args 不包含命令名
     * @param s 不为null
     * @return 转换后的值
     */
    private String convert(Player p, String[] args, String s) {
        if (s.length() >= 3 && s.charAt(0) == '{' && s.charAt(s.length()-1) == '}') {//{...}
            String content = s.substring(1, s.length()-1).toLowerCase();
            if (content.equals("name")) return p.getName();//{name}
            return UtilApi.convertArg(args, s);
        }else return s;
    }
    
    /**
     * (由配置文件)更新缓存
     */
    private void update() {
        cmdToGroups.clear();
        aliasToCmd.clear();
        
        for (GroupContext groupContext:config.getGroups().values()) {
            for (String cmd:groupContext.getCmds().keySet()) cmdToGroups.put(cmd, groupContext);
        }

        for (GroupContext groupContext:config.getGroups().values()) {
            for (Entry<String, CmdContext> entry:groupContext.getCmds().entrySet()) {
                //命令本身
                aliasToCmd.put(entry.getKey(), entry.getKey());
                //aliases
                for (String aliase:entry.getValue().getAliases()) aliasToCmd.put(aliase, entry.getKey());
            }
        }
    }

    private FancyMessage get(String player, int id, Object... args) {
        return config.getLang().get(player, id, args);
    }
}
