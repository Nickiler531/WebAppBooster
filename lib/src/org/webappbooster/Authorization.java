/*
 * Copyright 2012-2013, webappbooster.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class Authorization {

    final static private String              FILE_NAME         = "webappbooster-permissions";

    static private Map<String, List<String>> permissionsOnce   = null;
    static private Map<String, List<String>> permissionsAlways = null;

    static {
        permissionsOnce = new HashMap<String, List<String>>();
        readPermissions();
    }

    static private void readPermissions() {
        permissionsAlways = new HashMap<String, List<String>>();
        try {
            FileInputStream is = BoosterService.getService().openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(is);
            permissionsAlways = (Map<String, List<String>>) ois.readObject();
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
            FileOutputStream os = BoosterService.getService().openFileOutput(FILE_NAME,
                    Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(permissionsAlways);
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
            permissionsOnce.put(origin, p);
        } else {
            permissionsAlways.put(origin, p);
            writePermissions();
        }
    }

    static public String[] getPermissions(String origin) {
        List<String> p = permissionsOnce.get(origin);
        if (p == null) {
            p = permissionsAlways.get(origin);
        }
        if (p == null) {
            return new String[0];
        }
        return p.toArray(new String[p.size()]);
    }

    static public boolean checkOnePermission(String origin, String permission) {
        List<String> p = permissionsOnce.get(origin);
        if (p != null && p.contains(permission)) {
            return true;
        }
        p = permissionsAlways.get(origin);
        if (p != null) {
            return p.contains(permission);
        }
        return false;
    }

    static public boolean checkPermissions(String origin, String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (!checkOnePermission(origin, permissions[i])) {
                return false;
            }
        }
        return true;
    }

    static public void revokeOneTimePermissions(String origin) {
        if (permissionsOnce.containsKey(origin)) {
            permissionsOnce.remove(origin);
        }
    }

    static public void revokePermissions(String origin) {
        revokeOneTimePermissions(origin);
        if (permissionsAlways.containsKey(origin)) {
            permissionsAlways.remove(origin);
            writePermissions();
        }
    }
}
