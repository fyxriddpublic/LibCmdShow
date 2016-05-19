package com.fyxridd.lib.show.cmd.manager;

import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.config.ConfigManager;
import com.fyxridd.lib.func.api.FuncApi;
import com.fyxridd.lib.show.cmd.ShowPlugin;
import com.fyxridd.lib.show.cmd.config.CmdConfig;

public class CmdManager {
    public static final String CMD = "cmd";
    
    private CmdConfig config;

    public CmdManager() {
        //添加配置监听
        ConfigApi.addListener(ShowPlugin.instance.pn, CmdConfig.class, new ConfigManager.Setter<CmdConfig>() {
            @Override
            public void set(CmdConfig value) {
                config = value;
            }
        });
        //注册功能类型
        FuncApi.registerTypeHook(CMD , config.getCmdFuncPrefix());
    }
}
