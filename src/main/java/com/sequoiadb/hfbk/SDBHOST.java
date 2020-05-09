package com.sequoiadb.hfbk;

import java.util.HashMap;
import java.util.Map;

public class SDBHOST {
    public final static Map<String,String> map = new HashMap<>();
    static {
        map.put("sdb02", "192.168.232.136");
        map.put("sdb03", "192.168.232.137");
    }
}
