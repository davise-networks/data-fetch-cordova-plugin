var exec = require('cordova/exec');

exports.getAllSMS = function(arg0, success, error) {
    exec(success, error, "FetchData", "readAllSMS", [arg0]);
};

exports.getAllCalls = function(arg0, success, error) {
	exec(success, error, "FetchData", "readCallLogs", [arg0]);
}

exports.getAllContacts = function(arg0, success, error) {
	exec(success, error, "FetchData", "readContacts", [arg0]);
}