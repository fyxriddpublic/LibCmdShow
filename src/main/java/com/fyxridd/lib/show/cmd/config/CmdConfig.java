package com.fyxridd.lib.show.cmd.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import com.fyxridd.lib.core.api.config.basic.Path;
import com.fyxridd.lib.core.api.config.convert.ConfigConvert;
import com.fyxridd.lib.core.api.config.convert.ConfigConvert.ConfigConverter;
import com.fyxridd.lib.core.api.lang.LangConverter;
import com.fyxridd.lib.core.api.lang.LangGetter;
import com.fyxridd.lib.show.cmd.convert.Convert;
import com.fyxridd.lib.show.cmd.convert.TypeInfo;

public class CmdConfig {
    /**
     * 命令转换
     */
    private class Converter implements ConfigConverter<Map<String, Convert>> {
        @Override
        public Map<String, Convert> convert(String plugin, ConfigurationSection config) throws Exception {
            Map<String, Convert> map = new HashMap<>();
            for (String cmdName:config.getValues(false).keySet()) {
                //TypeInfo
                Map<Integer, TypeInfo> types = new HashMap<>();
                {
                    ConfigurationSection typesMs = config.getConfigurationSection(cmdName+".types");
                    int index = 0;
                    while (typesMs.contains(""+(++index))) {
                        ConfigurationSection typeMs = typesMs.getConfigurationSection(""+index);
                        int conArgs = typeMs.getInt("conArgs", -1);
                        String cmdStr = typeMs.getString("cmd", null);
                        String[] args = cmdStr != null?cmdStr.split(" "):null;
                        String per = typeMs.getString("per", null);
                        types.put(index, new TypeInfo(conArgs, args, per));
                    }
                }
                Convert convert = new Convert(types);
                //cmdName+aliases
                {
                    map.put(cmdName.toLowerCase(), convert);
                    for (String alias:config.getStringList(cmdName+".aliases")) map.put(alias.toLowerCase(), convert);
                }
            }
            return map;
        }
    }
    
    @Path("lang")
    @ConfigConvert(LangConverter.class)
    private LangGetter lang;
    
    @Path("cmdFuncPrefix")
    private String cmdFuncPrefix;
    
    @Path("convert.defaultDeny")
    private boolean convertDefaultDeny;
    //可能出现land与l对应同一个Convert实例的情况
    //命令名(小写) 命令信息
    @Path("convert.converts")
    @ConfigConvert(Converter.class)
    private Map<String, Convert> converts;

    public LangGetter getLang() {
        return lang;
    }

    public String getCmdFuncPrefix() {
        return cmdFuncPrefix;
    }

    public boolean isConvertDefaultDeny() {
        return convertDefaultDeny;
    }

    public Map<String, Convert> getConverts() {
        return converts;
    }
}
