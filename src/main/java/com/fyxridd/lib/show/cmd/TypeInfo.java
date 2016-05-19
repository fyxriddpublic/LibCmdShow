package com.fyxridd.lib.show.cmd;

public class TypeInfo {
    //-1表示无限制
    private int conArgs;
    //可为null,null表示不转换
    private String[] args;
    //可为null或空
    private String per;

    public TypeInfo(int conArgs, String[] args, String per) {
        this.conArgs = conArgs;
        this.args = args;
        this.per = per;
    }

    public int getConArgs() {
        return conArgs;
    }

    public String[] getArgs() {
        return args;
    }

    public String getPer() {
        return per;
    }
}
