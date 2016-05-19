package com.fyxridd.lib.show.cmd.convert;

import java.util.Map;

public class Convert {
    private Map<Integer, TypeInfo> types;

    public Convert(Map<Integer, TypeInfo> types) {
        this.types = types;
    }

    public Map<Integer, TypeInfo> getTypes() {
        return types;
    }
}
