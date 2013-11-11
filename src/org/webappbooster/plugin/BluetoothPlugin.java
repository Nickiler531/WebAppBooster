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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Base64;

@PluginMappingAnnotation(actions = "BLUETOOTH_DEVICES|BLUETOOTH_CONNECT|BLUETOOTH_READ|BLUETOOTH_WRITE|BLUETOOTH_DISCONNECT", permission = "BLUETOOTH")
public class BluetoothPlugin extends Plugin {

    private static class BluetoothThread extends Thread {

        private final static UUID GENERIC_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        private BluetoothSocket   mmSocket;
        private InputStream       mmInStream;
        private OutputStream      mmOutStream;

        private ArrayList<Byte>   readBuffer;

        private boolean           isConnected;

        public BluetoothThread(BluetoothDevice device) {
            isConnected = false;
            try {
                mmSocket = device.createRfcommSocketToServiceRecord(GENERIC_UUID);
                mmSocket.connect();
            } catch (IOException e) {
                return;
            }
            try {
                mmInStream = mmSocket.getInputStream();
                mmOutStream = mmSocket.getOutputStream();
            } catch (IOException e) {
                return;
            }
            readBuffer = new ArrayList<Byte>();
            isConnected = true;
        }

        public boolean isConnected() {
            return isConnected;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len;

            while (true) {
                if (!isConnected) {
                    break;
                }
                try {
                    len = mmInStream.read(buffer);
                    addBytesRead(buffer, len);
                } catch (IOException e) {
                    isConnected = false;
                    break;
                }
            }
        }

        private synchronized void addBytesRead(byte[] buffer, int len) {
            for (int i = 0; i < len; i++) {
                readBuffer.add(buffer[i]);
            }
            this.notify();
        }

        public synchronized byte[] read(int length) {
            if (!isConnected) {
                return null;
            }
            while (readBuffer.size() == 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    isConnected = false;
                    return null;
                }
            }
            int n = (length > readBuffer.size()) ? readBuffer.size() : length;
            byte[] data = new byte[n];
            for (int i = 0; i < n; i++) {
                data[i] = readBuffer.get(0);
                readBuffer.remove(0);
            }
            return data;
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            if (!isConnected) {
                return;
            }
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                isConnected = false;
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
            isConnected = false;
        }
    }

    private BluetoothAdapter             btAdapter;
    private Map<String, BluetoothThread> connections;

    @Override
    public void onCreate(String origin) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        connections = new HashMap<String, BluetoothThread>();
    }

    @Override
    public void onDestroy() {
        for (BluetoothThread thread : connections.values()) {
            thread.cancel();
        }
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

    private BluetoothThread getBluetoothThread(Request request) {
        String hostName = request.getString("hostName");
        BluetoothThread thread = connections.get(hostName);
        if (thread == null) {
            // We are not connected
            Response resp = request.createResponse(Response.ERR_CANCELLED);
            resp.send();
            return null;
        }
        return thread;
    }

    private void executeDisconnect(Request request) {
        BluetoothThread thread = getBluetoothThread(request);
        if (thread == null) {
            return;
        }
        Response resp = request.createResponse(Response.OK);
        resp.add("connected", thread.isConnected());
        thread.cancel();
        String hostName = request.getString("hostName");
        connections.remove(hostName);
        resp.send();
    }

    private void executeWrite(Request request) {
        BluetoothThread thread = getBluetoothThread(request);
        if (thread == null) {
            return;
        }
        String data = request.getString("data");
        byte[] bytes = Base64.decode(data, Base64.DEFAULT);
        thread.write(bytes);
        Response resp = request.createResponse(Response.OK);
        resp.add("connected", thread.isConnected());
        resp.send();
    }

    private void executeRead(Request request) {
        BluetoothThread thread = getBluetoothThread(request);
        if (thread == null) {
            return;
        }
        int len = request.getInt("length");
        byte[] data = thread.read(len);
        Response resp = request.createResponse(Response.OK);
        resp.add("connected", thread.isConnected());
        if (data != null) {
            resp.add("data", Base64.encodeToString(data, Base64.DEFAULT));
        }
        resp.send();
    }

    private void executeConnect(Request request) {
        String hostName = request.getString("hostName");
        if (connections.containsKey(hostName)) {
            // We are already connected
            Response resp = request.createResponse(Response.ERR_CANCELLED);
            resp.send();
            return;
        }

        // Find the paired device
        BluetoothThread thread = null;
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(hostName)) {
                btAdapter.cancelDiscovery();
                thread = new BluetoothThread(device);
                break;
            }
        }
        if (thread == null || !thread.isConnected()) {
            // Couldn't find paired device or couldn't connect to it
            Response resp = request.createResponse(Response.ERR_CANCELLED);
            resp.send();
            return;
        }
        connections.put(hostName, thread);
        thread.start();
        Response resp = request.createResponse(Response.OK);
        resp.add("connected", true);
        resp.send();
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
