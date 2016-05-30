package com.fyxridd.lib.show.cmd.config;

import java.io.File;
import java.util.*;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.basic.ListHelper;
import com.fyxridd.lib.core.api.config.basic.ListType;
import com.fyxridd.lib.core.api.config.basic.Path;
import com.fyxridd.lib.core.api.config.convert.ConfigConvert;
import com.fyxridd.lib.core.api.config.convert.ConfigConvert.ConfigConverter;
import com.fyxridd.lib.core.api.config.limit.Min;
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
    private static class GroupsConverter implements ConfigConverter<Map<String, GroupContext>> {
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
                                groups.put(group, loadGroupContext(group, f));
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
        
        private GroupContext loadGroupContext(String group, File f) throws Exception {
            YamlConfiguration config = UtilApi.loadConfigByUTF8(f);
            if (config == null) throw new Exception("yaml load error!");
            
            String name = config.getString("name");
            String desc = config.getString("desc");
            Map<String, CmdContext> cmds = new LinkedHashMap<>();
            
            {
                ConfigurationSection cmdsSection = config.getConfigurationSection("cmds");
                if (cmdsSection != null) {
                    for (String cmd:cmdsSection.getValues(false).keySet()) {
                        try {
                            cmds.put(cmd.toLowerCase(), loadCmdContext(cmd, cmdsSection.getConfigurationSection(cmd)));
                        } catch (Exception e) {
                            throw new Exception("load cmd '"+cmd+"' error: "+e.getMessage(), e);
                        }
                    }
                }   
            }
            
            return new GroupContext(group, name, desc, cmds);
        }

        private CmdContext loadCmdContext(String cmd, ConfigurationSection cmdSection) throws Exception {
            List<String> aliases = new ArrayList<>();
            for (String s:cmdSection.getStringList("aliases")) aliases.add(s.toLowerCase());
            Map<Integer, FuncContext> funcs = new LinkedHashMap<>();
            {
                ConfigurationSection funcsSection = cmdSection.getConfigurationSection("funcs");
                if (funcsSection != null) {
                    int index = 0;
                    while (funcsSection.contains(""+(++index))) {
                        try {
                            ConfigurationSection funcSection = funcsSection.getConfigurationSection(""+index);
                            funcs.put(index, loadFuncContext(cmd, funcSection));
                        } catch (Exception e) {
                            throw new Exception("load index "+index+" error: "+e.getMessage(), e);
                        }
                    }
                }   
            }
            return new CmdContext(cmd, aliases, funcs);
        }

        private FuncContext loadFuncContext(String cmd, ConfigurationSection funcSection) throws Exception {
            String useage = funcSection.getString("useage");
            String desc = funcSection.getString("desc");
            int argsLength = funcSection.getInt("argsLength");
            if (argsLength < -1) throw new Exception("argsLength can't less than -1");
            String per = funcSection.getString("per");
            String convert = funcSection.getString("convert");
            return new FuncContext(cmd, useage, desc, argsLength, per, convert);
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
    
    //help group
    @Path("help.group.pageSize")
    @Min(1)
    private int helpGroupPageSize;
    @Path("help.group.headers")
    @ListHelper(ListType.Integer)
    private List<Integer> helpGroupHeaders;
    @Path("help.group.bodys")
    @ListHelper(ListType.Integer)
    private List<Integer> helpGroupBodys;
    @Path("help.group.footers")
    @ListHelper(ListType.Integer)
    private List<Integer> helpGroupFooters;
    
    //help cmd
    @Path("help.cmd.pageSize")
    @Min(1)
    private int helpCmdPageSize;
    @Path("help.cmd.headers")
    @ListHelper(ListType.Integer)
    private List<Integer> helpCmdHeaders;
    @Path("help.cmd.bodys.hideNoPerFunc")
    private boolean helpCmdBodysHideNoPerFunc;
    @Path("help.cmd.bodys.cmds")
    @ListHelper(ListType.Integer)
    private List<Integer> helpCmdBodysCmds;
    @Path("help.cmd.bodys.funcs")
    @ListHelper(ListType.Integer)
    private List<Integer> helpCmdBodysFuncs;
    @Path("help.cmd.footers")
    @ListHelper(ListType.Integer)
    private List<Integer> helpCmdFooters;
    
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

    public int getHelpGroupPageSize() {
        return helpGroupPageSize;
    }

    public List<Integer> getHelpGroupHeaders() {
        return helpGroupHeaders;
    }

    public List<Integer> getHelpGroupBodys() {
        return helpGroupBodys;
    }

    public List<Integer> getHelpGroupFooters() {
        return helpGroupFooters;
    }

    public int getHelpCmdPageSize() {
        return helpCmdPageSize;
    }

    public List<Integer> getHelpCmdHeaders() {
        return helpCmdHeaders;
    }

    public boolean isHelpCmdBodysHideNoPerFunc() {
        return helpCmdBodysHideNoPerFunc;
    }

    public List<Integer> getHelpCmdBodysCmds() {
        return helpCmdBodysCmds;
    }

    public List<Integer> getHelpCmdBodysFuncs() {
        return helpCmdBodysFuncs;
    }

    public List<Integer> getHelpCmdFooters() {
        return helpCmdFooters;
    }
}
