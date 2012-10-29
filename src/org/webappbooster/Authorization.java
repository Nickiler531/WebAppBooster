package org.webappbooster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Authorization {

    static private Map<String, List<String>> permissions;

    static {
        permissions = new HashMap<String, List<String>>();
    }


    static public void setPermissions(String origin, String[] perm, boolean once) {
        List<String> p = new ArrayList<String>();
        Collections.addAll(p, perm);
        permissions.put(origin, p);
    }

    static public String[] getPermissions(String origin) {
        List<String> p = permissions.get(origin);
        if (p == null) {
            return new String[0];
        }
        return p.toArray(new String[p.size()]);
    }

    static public boolean checkPermission(String origin, String permission) {
        List<String> p = permissions.get(origin);
        if (p == null) {
            return false;
        }
        return p.contains(permission);
    }
    
    static public void revokePermissions(String origin) {
        permissions.remove(origin);
    }
}
