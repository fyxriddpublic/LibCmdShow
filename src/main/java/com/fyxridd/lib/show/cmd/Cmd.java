package com.fyxridd.lib.show.cmd;

import java.util.Map;

public class Cmd {
    private Map<Integer, TypeInfo> types;

    public Cmd(Map<Integer, TypeInfo> types) {
        this.types = types;
    }

    public Map<Integer, TypeInfo> getTypes() {
        return types;
    }
}
