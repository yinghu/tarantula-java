/**
 **   STAND ALONE WEB SOCKET JS
**/
const execSync = require('child_process').execSync;
execSync('npm install websocket');
execSync('npm install uuid');
var tp = require('./tarantula.web.socket.js');
tp.start();
process.on('SIGINT', () => {
    tp.stop();
});