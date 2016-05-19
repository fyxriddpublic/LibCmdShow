package com.fyxridd.lib.show.cmd.manager;

import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.config.ConfigManager;
import com.fyxridd.lib.show.cmd.Cmd;
import com.fyxridd.lib.show.cmd.ShowPlugin;
import com.fyxridd.lib.show.cmd.TypeInfo;
import com.fyxridd.lib.show.cmd.config.CmdConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.HashMap;
import java.util.Map;

public class CmdManager {
    private CmdConfig config;

    //可能出现land与l对应同一个Cmd实例的情况
    //命令名(小写) 命令信息
    private Map<String, Cmd> cmds;

    public CmdManager() {
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
                    String cmdName = args[0].toLowerCase();
                    //无配置
                    Cmd cmd = cmds.get(cmdName);
                    if (cmd == null) {
                        if (defaultDeny) {
                            event.setCancelled(true);
                            ShowApi.tip(event.getPlayer(), get(5), true);
                        }
                        return;
                    }
                    //无权限
                    int index = 0;
                    while (cmd.getTypes().containsKey(++index)) {
                        //检测条件
                        TypeInfo typeInfo = cmd.getTypes().get(index);
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
                            event.setMessage(result);
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
            return CoreApi.convertArg(args, s);
        }else return s;
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CmdPlugin.pn);

        //defaultDeny
        defaultDeny = config.getBoolean("defaultDeny");
        //cmds
        cmds = new HashMap<>();
        MemorySection cmdsMs = (MemorySection) config.get("cmds");
        for (String cmdName:cmdsMs.getValues(false).keySet()) {
            //TypeInfo
            MemorySection typesMs = (MemorySection) cmdsMs.get(cmdName+".types");
            int index = 0;
            HashMap<Integer, TypeInfo> types = new HashMap<>();
            while (typesMs.contains(""+(++index))) {
                MemorySection typeMs = (MemorySection) typesMs.get(""+index);
                int conArgs = typeMs.getInt("conArgs", -1);
                String cmdStr = typeMs.getString("cmd", null);
                String[] args = cmdStr != null?cmdStr.split(" "):null;
                String per = typeMs.getString("per", null);
                types.put(index, new TypeInfo(conArgs, args, per));
            }
            Cmd cmd = new Cmd(types);
            //cmdName+aliases
            cmds.put(cmdName.toLowerCase(), cmd);
            for (String alias:cmdsMs.getStringList(cmdName+".aliases")) cmds.put(alias.toLowerCase(), cmd);
        }
    }
}
