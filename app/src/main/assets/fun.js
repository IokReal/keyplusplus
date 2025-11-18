(function(){
    'use strict';
    if (window.funny === true) {
        return;
    }
    window.funny = true;
    const _setRequestHeader = XMLHttpRequest.prototype.setRequestHeader;
    const _open = XMLHttpRequest.prototype.open;
    const _send = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(...rest){
        if (this.canNotSend == true){
            return;
        }
        _send.call(this, ...rest)
    }
    XMLHttpRequest.prototype.open = function(method, url, ...rest){
        this._url = url;
        this.canNotSend = false;
        return _open.call(this, method, url, ...rest);
    };
    XMLHttpRequest.prototype.setRequestHeader = function(name, value){
        try {
            const pattern = /^https:\/\/household\.key\.rt\.ru\/api\/v2\/app\/devices\/.*\/open$/;
            if (this._url && pattern.test(this._url) && name === "Authorization") {
                console.log(value);
                this.canNotSendSend = true;
                AndroidBridge.send(value, this._url);
            }

        } catch (e) {
            console.warn('Header hook error', e);
        }
    return _setRequestHeader.call(this, name, value);
    };
})();