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

package org.webappbooster.plugin;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

@PluginMappingAnnotation(actions = "BLUETOOTH_DEVICES|BLUETOOTH_CONNECT|BLUETOOTH_READ|BLUETOOTH_WRITE|BLUETOOTH_DISCONNECT", permission = "BLUETOOTH")
public class BluetoothPlugin extends Plugin {

    private BluetoothAdapter btAdapter;

    @Override
    public void onCreate(String origin) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void execute(Request request) {
        if (btAdapter == null) {
            // Device does not have BT
            Response resp = request.createResponse(Response.ERR_NOT_AVAILABLE);
            resp.send();
            return;
        }
        String action = request.getAction();
        if (action.equals("BLUETOOTH_DEVICES")) {
            executeDevices(request);
            return;
        }
        if (!btAdapter.isEnabled()) {
            // BT or is disabled
            Response resp = request.createResponse(Response.ERR_NOT_AVAILABLE);
            resp.send();
            return;
        }
        if (action.equals("BLUETOOTH_CONNECT")) {
            executeConnect(request);
        } else if (action.equals("BLUETOOTH_READ")) {
            executeRead(request);
        } else if (action.equals("BLUETOOTH_WRITE")) {
            executeWrite(request);
        } else if (action.equals("BLUETOOTH_DISCONNECT")) {
            executeDisconnect(request);
        } else {
            Response resp = request.createResponse(Response.ERR_MALFORMED_REQUEST);
            resp.send();
        }
    }

    private void executeDisconnect(Request request) {
        // TODO Auto-generated method stub

    }

    private void executeWrite(Request request) {
        // TODO Auto-generated method stub

    }

    private void executeRead(Request request) {
        // TODO Auto-generated method stub

    }

    private void executeConnect(Request request) {
        // TODO Auto-generated method stub

    }

    private void executeDevices(Request request) {
        Response resp = request.createResponse(Response.OK);
        resp.add("bluetoothOn", btAdapter.isEnabled());
        if (btAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                JSONArray devicesJSON = new JSONArray();
                for (BluetoothDevice device : pairedDevices) {
                    try {
                        JSONObject deviceJSON = new JSONObject();
                        deviceJSON.put("displayName", device.getName());
                        deviceJSON.put("hostName", device.getAddress());
                        devicesJSON.put(deviceJSON);
                    } catch (JSONException e) {
                    }
                }
                resp.add("devices", devicesJSON);
            }
        }
        resp.send();
    }
}
