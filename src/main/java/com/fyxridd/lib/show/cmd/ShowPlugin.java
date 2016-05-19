package com.fyxridd.lib.show.cmd;

import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.plugin.SimplePlugin;
import com.fyxridd.lib.show.cmd.config.CmdConfig;
import com.fyxridd.lib.show.cmd.manager.CmdManager;

public class ShowPlugin extends SimplePlugin {
    public static ShowPlugin instance;

    private CmdManager cmdManager;

    //启动插件
    @Override
    public void onEnable() {
        instance = this;

        //注册配置
        ConfigApi.register(pn, CmdConfig.class);

        cmdManager = new CmdManager();

        super.onEnable();
    }

    public CmdManager getCmdManager() {
        return cmdManager;
    }
}