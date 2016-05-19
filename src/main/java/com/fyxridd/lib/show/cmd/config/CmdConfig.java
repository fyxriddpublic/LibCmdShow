package com.fyxridd.lib.show.cmd.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.basic.Path;
import com.fyxridd.lib.core.api.config.convert.ConfigConvert;
import com.fyxridd.lib.core.api.config.convert.ConfigConvert.ConfigConverter;
import com.fyxridd.lib.core.api.lang.LangConverter;
import com.fyxridd.lib.core.api.lang.LangGetter;
import com.fyxridd.lib.show.cmd.ShowPlugin;
import com.fyxridd.lib.show.cmd.context.CmdContext;
import com.fyxridd.lib.show.cmd.context.FuncContext;
import com.fyxridd.lib.show.cmd.context.GroupContext;

public class CmdConfig {
    private static final String GROUP_DIR_NAME = "groups";
    private static final String CMD_FILE_SUFFIX= ".yml";
    
    /**
     * 命令转换
     */
    private class GroupsConverter implements ConfigConverter<Map<String, GroupContext>> {
        @Override
        public Map<String, GroupContext> convert(String plugin, ConfigurationSection config) throws Exception {
            Map<String, GroupContext> groups = new HashMap<>();
            
            File dir = new File(ShowPlugin.instance.dataPath, GROUP_DIR_NAME);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f:files) {
                        if (f.getName().endsWith(CMD_FILE_SUFFIX)) {
                            String group = f.getName().substring(0, f.getName().length()-CMD_FILE_SUFFIX.length());
                            try {
                                groups.put(group, loadGroupContext(f));
                            } catch (Exception e) {
                                throw new Exception("load group '"+group+"' error: "+e.getMessage(),e);
                            }
                        }
                    }
                }
            }
            
            //返回
            return groups;
        }
        
        private GroupContext loadGroupContext(File f) throws Exception {
            YamlConfiguration config = UtilApi.loadConfigByUTF8(f);
            if (config == null) throw new Exception("yaml load error!");
            
            String name = config.getString("name");
            String desc = config.getString("desc");
            Map<String, CmdContext> cmds = new HashMap<>();
            
            {
                ConfigurationSection cmdsSection = config.getConfigurationSection("cmds");
                if (cmdsSection != null) {
                    for (String cmd:cmdsSection.getValues(false).keySet()) {
                        try {
                            cmds.put(cmd.toLowerCase(), loadCmdContext(cmdsSection.getConfigurationSection(cmd)));
                        } catch (Exception e) {
                            throw new Exception("load cmd '"+cmd+"' error: "+e.getMessage(), e);
                        }
                    }
                }   
            }
            
            return new GroupContext(name, desc, cmds);
        }

        private CmdContext loadCmdContext(ConfigurationSection cmdSection) throws Exception {
            List<String> aliases = new ArrayList<>();
            for (String s:cmdSection.getStringList("aliases")) aliases.add(s.toLowerCase());
            Map<Integer, FuncContext> funcs = new HashMap<>();
            {
                ConfigurationSection funcsSection = cmdSection.getConfigurationSection("funcs");
                if (funcsSection != null) {
                    int index = 0;
                    while (funcsSection.contains(""+(++index))) {
                        try {
                            ConfigurationSection funcSection = funcsSection.getConfigurationSection(""+index);
                            funcs.put(index, loadFuncContext(funcSection));
                        } catch (Exception e) {
                            throw new Exception("load index "+index+" error: "+e.getMessage(), e);
                        }
                    }
                }   
            }
            return new CmdContext(aliases, funcs);
        }

        private FuncContext loadFuncContext(ConfigurationSection funcSection) throws Exception {
            String useage = funcSection.getString("useage");
            String desc = funcSection.getString("desc");
            int argsLength = funcSection.getInt("argsLength");
            if (argsLength < -1) throw new Exception("argsLength can't less than -1");
            String per = funcSection.getString("per");
            String convert = funcSection.getString("convert");
            return new FuncContext(useage, desc, argsLength, per, convert);
        }
    }
    
    @Path("lang")
    @ConfigConvert(LangConverter.class)
    private LangGetter lang;
    
    @Path("cmdFuncPrefix")
    private String cmdFuncPrefix;
    
    @Path("convert.defaultDeny")
    private boolean convertDefaultDeny;

    @Path("")
    @ConfigConvert(GroupsConverter.class)
    private Map<String, GroupContext> groups;
    
    public LangGetter getLang() {
        return lang;
    }

    public String getCmdFuncPrefix() {
        return cmdFuncPrefix;
    }

    public boolean isConvertDefaultDeny() {
        return convertDefaultDeny;
    }

    public Map<String, GroupContext> getGroups() {
        return groups;
    }
}
