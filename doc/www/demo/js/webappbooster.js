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

var WebAppBooster = {

    URI: "ws://localhost:8042",
    
    OK: 0,
    CLOSED: 1,
    ERR_PERMISSION_DENIED: -1,
    ERR_WEBSOCK_NOT_AVAILABLE: -2,
    ERR_WEBSOCK_ACCESS_DENIED: -3,
    ERR_WEBSOCK_NOT_CONNECTED: -4,
    ERR_AUTHENTICATION_REQUIRED: -5,

    PERMISSION_READ_CONTACTS: "READ_CONTACTS",
    PERMISSION_GYRO: "GYRO",
    PERMISSION_ACCELEROMETER: "ACCELEROMETER",
    PERMISSION_AUDIO: "AUDIO",
    PERMISSION_GALLERY: "GALLERY",
    
    _nextRequestId: 0,
    _requestIdMap: {},
    _ws: 0,
    _token: 0,
    
    open: function(path, cb) {
        if (this._ws != 0) {
            cb({status: WebAppBooster.OK});
            return;
        }
        if (!window.WebSocket) {
            cb({status: this.ERR_WEBSOCK_NOT_AVAILABLE});
            return;
        }
        try {
            this._ws = new WebSocket(this.URI);
            this._ws.onopen = function () {
                var localStorageKey = "webappbooster_token";
                var h = window.location.href;
                var m = h.match(/#webappbooster_token=([^&]*)/);
                var token;
                if (m) {
                    token = decodeURIComponent(m[1].replace(/\+/g, " "));
                    window.localStorage[localStorageKey] = token;
                    h = h.replace(/(#webappbooster_token=[^&]*)/, "");
                    window.location.replace(h);
                } else
                    token = window.localStorage[localStorageKey];
                function authenticate() {
                    window.localStorage.removeItem(localStorageKey);
                    var req = {
                        action: "REQUEST_AUTHENTICATION",
                        path: path
                    };
                    WebAppBooster._sendRequest(req, function () { }, 0);
                    cb({ status: WebAppBooster.ERR_AUTHENTICATION_REQUIRED });
                }
                function success() {
                    WebAppBooster._token = token;
                    cb({ status: WebAppBooster.OK });
                }
                if (token) {
                    var req = {
                        action: "AUTHENTICATE",
                        token: token
                    };
                    WebAppBooster._sendRequest(req, function (res) {
                        if (res.status == WebAppBooster.OK) {
                            success();
                        }
                        else {
                            authenticate();
                        }
                    }, 0);
                }
                else {
                    authenticate();
                }
            };
            this._ws.onmessage = this._onmessage;
            this._ws.onclose = function() {
                WebAppBooster._ws = 0;
                cb({status: WebAppBooster.CLOSED});
            };
        } catch (e) {
            WebAppBooster._ws = 0;
            cb({ status: WebAppBooster.ERR_WEBSOCK_ACCESS_DENIED });
        }
    },

    _onmessage: function (e) {
        try {
            var resp = JSON.parse(e.data);
            if ("id" in resp) {
                var id = "id" + resp.id;
                var m = WebAppBooster._requestIdMap[id];
                var cb = m[0];
                var keep_cb = m[1];
                if (!keep_cb) {
                    delete WebAppBooster._requestIdMap[id];
                }
                cb(resp);
            }
        } catch(ex) {}
    },
    
    _sendRequest: function(req, cb, keep_cb) {
        if (this._ws == 0) {
            cb({status: WebAppBooster.ERR_WEBSOCK_NOT_CONNECTED});
            return;
        }
        req.id = this._nextRequestId++;
        this._requestIdMap["id" + req.id] = [cb, keep_cb];
        this._ws.send(JSON.stringify(req));
    },
    
    requestPermissions: function(permissions, cb) {
        var req = {action: "REQUEST_PERMISSIONS",
                   permissions: permissions};
        this._sendRequest(req, cb, 0);
    },
    
    pickContact: function(cb) {
        var req = {action: "PICK_CONTACT"};
        this._sendRequest(req, cb, 0);
    },
    
    startGyro: function(cb) {
        var req = {action: "START_GYRO"};
        this._sendRequest(req, cb, 1);
    },

    stopGyro: function(cb) {
        var req = {action: "STOP_GYRO"};
        this._sendRequest(req, function(resp) {
            // Delete the callback that was registered with START_GYRO
            delete WebAppBooster._requestIdMap["id" + resp.startId];
            cb(resp);
        }, 0);
    },

    startAccelerometer: function(cb) {
        var req = {action: "START_ACCELEROMETER"};
        this._sendRequest(req, cb, 1);
    },

    stopAccelerometer: function(cb) {
        var req = {action: "STOP_ACCELEROMETER"};
        this._sendRequest(req, function(resp) {
            // Delete the callback that was registered with START_ACCELEROMETER
            delete WebAppBooster._requestIdMap["id" + resp.startId];
            cb(resp);
        }, 0);
    },
    
    listSongs: function(cb) {
    	var req = {action: "LIST_SONGS"};
    	this._sendRequest(req, cb, 0);
    },
    
    listImages: function(cb) {
    	var req = {action: "LIST_IMAGES"};
    	this._sendRequest(req, cb, 0);
    }
};