var exec = require("cordova/exec");
var PLUGIN_NAME = "GoogleAuthentication";

module.exports = {

    signInWithGoogle: function(success, error) {
        exec(success, error, PLUGIN_NAME, "signInWithGoogle", []);
    }
};