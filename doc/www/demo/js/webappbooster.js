
var WebAppBooster = {

    URI: "ws://localhost:8042",
    
    OK: 0,
    ERR_PERMISSION_DENIED: -1,
    ERR_WEBSOCK_NOT_AVAILABLE: -2,

    PERMISSION_READ_CONTACTS: "READ_CONTACTS",
    PERMISSION_GYRO: "GYRO",
    
    _nextRequestId: 0,
    _requestIdMap: {},
    _ws: 0,
    
    open: function(cb) {
        if (this._ws != 0) {
            cb({status: WebAppBooster.OK});
            return;
        }
        if (!window.WebSocket) {
            cb({status: this.ERR_WEBSOCK_NOT_AVAILABLE});
            return;
        }
        this._ws = new WebSocket(this.URI);
        this._ws.onopen = function() {
            cb({status: WebAppBooster.OK});
        };
        this._ws.onmessage = this._onmessage;
        this._ws.onclose = function() {
            WebAppBooster._ws = 0;
        };      
    },

    _onmessage: function(e) {
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
    }
};
