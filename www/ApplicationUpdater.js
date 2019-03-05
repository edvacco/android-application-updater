var exec = require('cordova/exec');

var PLUGIN_NAME = 'ApplicationUpdater';
var ApplicationUpdater = {
    update: function(onSuccess, onError, remoteUrl) {
        exec(onSuccess, onError, PLUGIN_NAME, 'update', [remoteUrl]);
    }
}

module.exports = ApplicationUpdater;