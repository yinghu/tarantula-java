/**
 **   WINDOWS TARANTULA BOOTSTRAP JS
**/
const execSync = require('child_process').execSync;
const { spawn } = require('child_process');
const bat = spawn('cmd.exe', ['/c', 'tarantula.bat']);

bat.stdout.on('data', (data) => {
  console.log(data.toString());
});

bat.stderr.on('data', (data) => {
  console.log(data.toString());
});
execSync('npm install websocket');
execSync('npm install uuid');

var tp = require('./tarantula.web.socket.js');
tp.start();
process.on('SIGINT', () => {
    tp.stop();
    bat.kill('SIGINT');
});