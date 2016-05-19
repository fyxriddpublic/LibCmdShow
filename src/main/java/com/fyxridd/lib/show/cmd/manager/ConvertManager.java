package com.fyxridd.lib.show.cmd.manager;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.core.config.ConfigManager;
import com.fyxridd.lib.show.cmd.ShowPlugin;
import com.fyxridd.lib.show.cmd.config.CmdConfig;
import com.fyxridd.lib.show.cmd.convert.Convert;
import com.fyxridd.lib.show.cmd.convert.TypeInfo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.EventExecutor;

public class ConvertManager {
    private CmdConfig config;

    public ConvertManager() {
        //添加配置监听
        ConfigApi.addListener(ShowPlugin.instance.pn, CmdConfig.class, new ConfigManager.Setter<CmdConfig>() {
            @Override
            public void set(CmdConfig value) {
                config = value;
            }
        });
        //注册事件
        {
            //命令事件
            Bukkit.getPluginManager().registerEvent(PlayerCommandPreprocessEvent.class, ShowPlugin.instance, EventPriority.LOWEST, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event e) throws EventException {
                    PlayerCommandPreprocessEvent event = (PlayerCommandPreprocessEvent) e;
                    //没有命令名,无法处理
                    if (event.getMessage().length() <= 1) return;
                    //解析
                    String[] args = event.getMessage().substring(1).split(" ");
                    String[] argsParam = new String[args.length-1];
                    if (args.length > 1) System.arraycopy(args, 1, argsParam, 0, args.length - 1);
                    String cmdName = args[0];
                    //无配置
                    Convert convert = config.getConverts().get(cmdName.toLowerCase());
                    {
                        if (convert == null) {
                            if (config.isConvertDefaultDeny()) {//无配置的禁止
                                event.setCancelled(true);
                                MessageApi.send(event.getPlayer(), get(event.getPlayer().getName(), 5), true);
                            }
                            return;
                        }  
                    }
                    //有配置
                    int index = 1;
                    TypeInfo typeInfo;
                    while ((typeInfo = convert.getTypes().get(index++)) != null) {
                        //检测条件
                        if (typeInfo.getConArgs() == -1 || typeInfo.getConArgs() == argsParam.length) {
                            //检测权限
                            if (!PerApi.has(event.getPlayer().getName(), typeInfo.getPer())) {
                                event.setCancelled(true);
                                return;
                            }
                            //不转换
                            if (typeInfo.getArgs() == null || typeInfo.getArgs().length == 0) return;
                            //命令转换
                            String result = "";
                            boolean first = true;
                            for (String s:typeInfo.getArgs()) {
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
                            break;
                        }
                    }
                }
            }, ShowPlugin.instance, true);
        }
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

    private FancyMessage get(String player, int id, Object... args) {
        return config.getLang().get(player, id, args);
    }
}
