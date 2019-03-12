var exec = require('cordova/exec');

var PLUGIN_NAME = 'ApplicationUpdater';
var ApplicationUpdater = {
    isUpdateAvailable: function(onSuccess, onError, remoteUrl) {
        exec(onSuccess, onError, PLUGIN_NAME, 'check', [remoteUrl]);
    },
    installUpdate: function(onSuccess, onError) {
        exec(onSuccess, onError, PLUGIN_NAME, 'install', []);
    }

}

module.exports = ApplicationUpdater;