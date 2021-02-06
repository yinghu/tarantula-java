/**
    TARANTULA PLATFORM NODE JS WEBSOCKET DAEMON
**/
const WebSocketServer = require('websocket').server;
const fs = require('fs');
var  cfg;
if (fs.existsSync('/etc/tarantula/ws.config')) {
    cfg = JSON.parse(fs.readFileSync('/etc/tarantula/ws.config','utf-8'));
}
else{
    cfg = JSON.parse(fs.readFileSync('ws.config','utf-8'));
}
var wcc = cfg[cfg.tarantula.name];
var conn = cfg.connection;
if(fs.existsSync('/etc/tarantula/ip.txt')){
    conn.host = fs.readFileSync('/etc/tarantula/ip.txt','utf-8');//replace ip from ip.txt
    conn.server.host = conn.host;
    conn.server.binding = conn.host;
}
conn.secured = wcc.protocol === 'https';
conn.server.secured = conn.secured; 
conn.path = 'tictactoe';
conn.server.path = conn.path;
conn.maxConnections = cfg.tarantula.maxConnections;
const http = require(wcc.protocol);
const querystring = require('querystring');
const {v1: uuidv1} = require('uuid');
const cMap = new Map(); //connectionId/game room mapping 
var web;
var serverId = uuidv1();
if(wcc.protocol==='https'){
    const options = {
      key: fs.readFileSync('/etc/tarantula/private.pem'),
      cert: fs.readFileSync('/etc/tarantula/certificate.pem')
    };
    web = http.createServer(options);
}
else{
    web = http.createServer();
}

/*
    KICK OFF HTTP REQUEST
*/
web.on('request',(req,res)=>{
  res.writeHead(404);
  res.end();
});

var wsServer = new WebSocketServer({
    httpServer: web,
    autoAcceptConnections: false
});
function validateOrigin(origin) {
    return true;
}

wsServer.on('request', function(request) {
    if (!validateOrigin(request.origin)) {
      request.reject();
      return;
    }
    validateOnTarantula(request.resource,(auth)=>{
        if(auth.successful&&cMap.has(auth.connectionId)){
            console.log(auth);
            var connection = request.accept('tarantula-service', request.origin);
            connection.connectionId = auth.connectionId;
            connection.player = auth.player;
            let room = cMap.get(auth.connectionId);
            room.join(connection);
            connection.on('message', function(message) {
                if (message.type === 'utf8') {
                    let room = cMap.get(connection.connectionId);
                    room.onMessage(connection,message.utf8Data);
                }
                else if (message.type === 'binary') {
                     //disconnect on binary payload
                }
            });
            connection.on('error',function(err){
                console.log('Error on web socket connection->'+err);       
            });
            connection.on('close', function(reasonCode, description) {
                let room = cMap.get(connection.connectionId);
                room.leave(connection);
                console.log('Peer closed from /'+reasonCode+"/"+description+"/"+ connection.remoteAddress +'/'+connection.connectionId);
            });
        }
        else{
            console.log('Ticket failed to validate');
            request.reject();//failure on ticket validation
        }
    });
});

exports.start=()=>{
    startOnTarantula((resp)=>{
        if(resp.successful){
            cfg.serverKey = resp.serverKey;
            createServerPushRoom(0);
            resp.connections.forEach(c => {
                createRoom(c);
            });
            web.listen(conn.port,()=>{
                console.log("Web socket is started on ["+conn.port+"] on TSL (HTTPS)["+(wcc.protocol==='https')+"]");
            });
            setInterval(ackOnTarantula,cfg.tarantula.ackTimeout);
            connectOnTarantula(f=>{
                createRoom(f.connectionId);
            });
        }
        else{
            console.log("validation failed");
            process.exit(1);
        }
    })
};

exports.stop=()=>{
    stopOnTarantula(()=>{
        console.log("closing web socket and service connection");
        wsServer.closeAllConnections();
        wsServer.shutDown();
        process.exit(1);
    });
};
function createServerPushRoom(connectionId){
    let room ={totalJoined:0,connections:[]};
    room.join = (connection)=>{
                console.log("server push enter->"+connection.connectionId);
            };
    room.leave = (connection)=>{
                console.log("server push leave->"+connection.connectionId);
            };
    room.onMessage = (connection,message)=>{
                console.log("server push message->"+message+" from "+connection.connectionId);
            };           
    cMap.set(connectionId,room);
}
function createRoom(connectionId){
    let room ={totalJoined:0,connections:[]};
    room.join = (connection)=>{
                let len = room.connections.push(connection);
                connection.index = len-1;
                connection.open = true;
                room.totalJoined++;
                connection.sendUTF('join'+JSON.stringify({message:'joined',seat:connection.index,player:connection.player}));
            };
    room.leave = (connection)=>{
                room.connections[connection.index].open=false;
                room.totalJoined--;
                room.connections.forEach(c=>{
                    if(c.open){
                        c.sendUTF('left'+JSON.stringify({message:'left',seat:connection.index}));
                    }
                });
            };
    room.onMessage = (connection,message)=>{
                room.connections.forEach(c=>{
                    if(c.open){
                        let jms = JSON.parse(message);
                        jms.sender = connection.index;
                        c.sendUTF(jms.label+JSON.stringify(jms));
                    }
                });
            };           
    cMap.set(connectionId,room);
}
function startOnTarantula(callback){
    conn.serverId = serverId;
    var data = JSON.stringify(conn);
    const headers = {'Tarantula-access-key':wcc.accessKey,'Tarantula-server-id':serverId,'Tarantula-action':'onStart'};
    postOnTarantula(wcc.path,headers,data,callback);
}
function connectOnTarantula(callback){
    const headers = {'Tarantula-connection-id':(cfg.tarantula.maxConnections+1),'Tarantula-access-key':wcc.accessKey,'Tarantula-server-id':serverId,'Tarantula-action':'onConnection'};
    getOnTarantula(wcc.path,headers,callback);
}
function ackOnTarantula(){
    const headers = {'Tarantula-access-key':wcc.accessKey,'Tarantula-server-id':serverId,'Tarantula-action':'onAck'};
    getOnTarantula(wcc.path,headers,()=>{});
}

function stopOnTarantula(callback){
    const headers = {'Tarantula-access-key':wcc.accessKey,'Tarantula-server-id':serverId,'Tarantula-action':'onStop'};
    getOnTarantula(wcc.path,headers,callback);
}

function validateOnTarantula(url,callback){
    var _payload = querystring.parse(url.substring(url.indexOf('?')+1));
    _payload.stub = _payload.stub/1;
    const data = JSON.stringify(_payload);
    const headers = {'Tarantula-magic-key':_payload.systemId,'Tarantula-tag':'index/user','Tarantula-action':'onTicket'};
    postOnTarantula('/user/action',headers,data,(resp)=>{
        resp.connectionId = _payload.connectionId/1;
        resp.player = _payload.systemId;
        callback(resp);
    });
}

function postOnTarantula(path,headers,data,callback){
    headers.Accept = 'application/json';
    headers['Content-type'] = 'application/x-www-form-urlencoded';
    headers['Content-length'] = data.length;
    const optsx ={
        rejectUnauthorized: false,
        secureProtocol: "TLSv1_2_method",
        hostname:wcc.host,port:wcc.port,
        path:path,method:'POST',
        headers:headers
    };
    const req = http.request(optsx,(res)=>{
        let resp=[];
        res.on('data', (data) => {
            for(var i = 0; i < data.length; ++i){
                resp.push(String.fromCharCode(data[i]));
            }
        });
        res.on('end', () => {
            callback(JSON.parse(resp.join('')));
        });
    });
    req.on('error', (e) => {
        console.log(e.message);
    });
    req.write(data);
    req.end();
}

function getOnTarantula(path,headers,callback){
    headers.Accept = 'application/json';
    headers['Content-type'] = 'application/x-www-form-urlencoded';
    const optsx ={
        rejectUnauthorized: false,
        secureProtocol: "TLSv1_2_method",
        hostname:wcc.host,port:wcc.port,
        path:path,method:'GET',
        headers:headers
    };
    const req = http.request(optsx,(res)=>{
        let resp=[];
        res.on('data', (data) => {
            for(var i = 0; i < data.length; ++i){
                resp.push(String.fromCharCode(data[i]));
            }
        });
        res.on('end', () => {
            callback(JSON.parse(resp.join('')));
        });
    });
    req.on('error', (e) => {
        console.log(e.message);
    });
    req.end();
}

