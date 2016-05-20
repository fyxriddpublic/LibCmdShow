package com.fyxridd.lib.show.cmd.context;

/**
 * 功能上下文
 */
public class FuncContext {
    private String cmd;

    //用法(可为null表示无用法)
    private String useage;
    
    //描述(可为null表示无描述)
    private String desc;
    
    //条件,变量数量,>=0(不包括命令名)(可选,默认-1,-1表示无限制)
    private int argsLength;
    
    //权限(可为null表示无权限需求)
    private String per;
    
    //命令转换(可为null表示不转换)
    private String convert;
    //为了提高效率
    //与convert同步(可为null)
    private String[] convertArgs;

    public FuncContext(String cmd, String useage, String desc, int argsLength, String per, String convert) {
        super();
        this.cmd = cmd;
        this.useage = useage;
        this.desc = desc;
        this.argsLength = argsLength;
        this.per = per;
        this.convert = convert;
        this.convertArgs = convert != null?(convert.split(" ")):null;
    }

    public String getCmd() {
        return cmd;
    }

    public String getUseage() {
        return useage;
    }

    public String getDesc() {
        return desc;
    }

    public int getArgsLength() {
        return argsLength;
    }

    public String getPer() {
        return per;
    }

    public String getConvert() {
        return convert;
    }

    public String[] getConvertArgs() {
        return convertArgs;
    }
}
