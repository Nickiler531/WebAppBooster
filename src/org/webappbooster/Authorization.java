package org.webappbooster;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Authorization {

    final static private String              FILE_NAME       = "webappbooster-permissions";

    static private Map<String, List<String>> permissionsTemp = null;
    static private Map<String, List<String>> permissionsPers = null;
    static private Context                   context         = null;


    static public void init(Context c) {
        context = c;
        permissionsTemp = new HashMap<String, List<String>>();
        readPermissions();
    }

    static private void readPermissions() {
        permissionsPers = new HashMap<String, List<String>>();
        try {
            FileInputStream is = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(is);
            permissionsPers = (Map<String, List<String>>) ois.readObject();
            ois.close();
            is.close();
        } catch (FileNotFoundException e) {
        } catch (StreamCorruptedException e) {
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
    }

    static private void writePermissions() {
        try {
            FileOutputStream os = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(permissionsPers);
            oos.close();
            os.close();
        } catch (FileNotFoundException e) {
        } catch (StreamCorruptedException e) {
        } catch (IOException e) {
        }
    }

    static public void setPermissions(String origin, String[] perm, boolean once) {
        List<String> p = new ArrayList<String>();
        Collections.addAll(p, perm);

        if (once) {
            permissionsTemp.put(origin, p);
        } else {
            permissionsPers.put(origin, p);
            writePermissions();
        }
    }

    static public String[] getPermissions(String origin) {
        List<String> p = permissionsTemp.get(origin);
        if (p == null) {
            p = permissionsPers.get(origin);
        }
        if (p == null) {
            return new String[0];
        }
        return p.toArray(new String[p.size()]);
    }

    static public boolean checkOnePermission(String origin, String permission) {
        List<String> p = permissionsTemp.get(origin);
        if (p == null) {
            p = permissionsPers.get(origin);
        } else {
            if (p.remove(permission)) {
                permissionsTemp.put(origin, p);
            }
        }
        if (p == null) {
            return false;
        }
        return p.contains(permission);
    }

    static public boolean checkPermissions(String origin, String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (!checkOnePermission(origin, permissions[i])) {
                return false;
            }
        }
        return true;
    }

    static public void revokePermissions(String origin) {
        if (permissionsTemp.containsKey(origin)) {
            permissionsTemp.remove(origin);
        }
        if (permissionsPers.containsKey(origin)) {
            permissionsPers.remove(origin);
            writePermissions();
        }
    }
}
