/**
    TARANTULA PLATFORM NODE JS WEBSOCKET DAEMON
**/
const WebSocketServer = require('websocket').server;
const fs = require('fs');
var  cfg;
if (fs.existsSync('/etc/tarantula/ws.config')) {
    cfg = JSON.parse(fs.readFileSync('/etc/tarantula/ws.config','utf-8'));
    console.log("using external configuration");
}
else{
    cfg = JSON.parse(fs.readFileSync('ws.config','utf-8'));
    console.log("using default configuration");
}
var cfgname = cfg.tarantula.name;
var wcc = cfg[cfgname];
var conn = cfg.connection;
if(fs.existsSync('/etc/tarantula/ip.txt')){
    conn.host = fs.readFileSync('/etc/tarantula/ip.txt','utf-8');//replace ip from ip.txt
    conn.server.host = conn.host;
    conn.server.binding = conn.host;
}
conn.secured = wcc.protocol === 'https';
conn.server.secured = conn.secured; 
conn.maxConnections = cfg.tarantula.maxConnections;
console.log("configuring connection with ["+conn.host+":"+conn.port+"]["+conn.secured+"]");
const http = require(wcc.protocol);
const querystring = require('querystring');
const {v1: uuidv1} = require('uuid');
const cMap = new Map(); //web socket client mapping clientId => connection
const pMap = new Map(); //server push event mapping label ==> updated payload
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
    console.log("Origin->"+origin);
    return true;
}

wsServer.on('request', function(request) {
    if (!validateOrigin(request.origin)) {
      request.reject();
      return;
    }
    validateOnTarantula(request.resource,(auth)=>{
        if(auth.successful){
            var connection = request.accept('tarantula-service', request.origin);
            connection.precence = auth.presence;
            connection.clientId = uuidv1();
            cMap.set(connection.clientId,connection);
            connection.sendUTF(JSON.stringify({message:'accepted connection',clientId:connection.clientId}));
            connection.on('message', function(message) {
                if (message.type === 'utf8') {
                    var _pr = fromString(message.utf8Data);
                    if(_pr.path){
                        actionOnTarantula(connection,_pr);
                    }
                    else{//register server push event such as notification
                        if(!pMap.has(_pr.label)){//register gameId as server push event
                           pMap.set(_pr.label,{listeners:[]});
                        }
                        let pse = pMap.get(_pr.label);
                        if(_pr.streaming){
                            //register callback if streaming required otherwise send back per request
                            if(_pr.action === 'onStart'){
                                connection.gameId = _pr.label;
                                //console.log('register on ['+_pr.label+'] from ['+connection.clientId+']');
                                pse.listeners.push(connection.clientId);
                                //connection.sendUTF(pse.payload);
                            }
                            else if(_pr.action ==='onStop'){
                                let ix = pse.listeners.indexOf(connection.clientId);
                                if(ix>=0){
                                    //console.log('unregister on ['+_pr.label+'] from ['+connection.clientId+'/'+ix+']');
                                    pse.listeners.splice(ix,1);
                                }
                            }
                        }
                    }
                }
                else if (message.type === 'binary') {
                     //disconnect on binary payload
                }
            });
            /**
            connection.on('frame',(fm)=>{
                
            });**/
            connection.on('error',function(err){
                //console.log('Error on web socket connection');
                cMap.delete(connection.clientId);
                let pse = pMap.get(connection.gameId);
                let ix = pse.listeners.indexOf(connection.clientId);
                if(ix>=0){
                    //console.log('unregister on ['+connection.gameId+'] from ['+connection.clientId+'/'+ix+']');
                    pse.listeners.splice(ix,1);
                }
            });
            connection.on('close', function(reasonCode, description) {
                cMap.delete(connection.clientId);
                let pse = pMap.get(connection.gameId);
                let ix = pse.listeners.indexOf(connection.clientId);
                if(ix>=0){
                    //console.log('unregister on ['+connection.gameId+'] from ['+connection.clientId+'/'+ix+']');
                    pse.listeners.splice(ix,1);
                }
                //console.log('Peer closed from /'+reasonCode+"/"+description+"/"+ connection.remoteAddress +'/'+connection.gameId);
            });
        }
        else{
            console.log('Ticket failed to validate');
            request.reject();//failure on ticket validation
        }
    });
});

exports.start=()=>{
    startOnTarantula(()=>{
        web.listen(conn.port,()=>{
            console.log("Web socket is started on ["+conn.port+"] on TSL (HTTPS)["+(wcc.protocol==='https')+"]");
        });
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

function startOnTarantula(callback){
    conn.serverId = serverId;
    var data = JSON.stringify(conn);
    const headers = {Accept:'application/json','Content-type':'application/x-www-form-urlencoded','Content-length':data.length,'Tarantula-access-key':wcc.accessKey,'Tarantula-server-id':serverId,'Tarantula-action':'onStart'};
    const optsx ={rejectUnauthorized: false,secureProtocol: "TLSv1_2_method",hostname:wcc.host,port:wcc.port,path:'/'+wcc.path,method:'POST',headers:headers};
    const req = http.request(optsx,(res)=>{
        var resp=[];
        res.on('data', (data) => {
            for(var i = 0; i < data.length; ++i){
                resp.push(String.fromCharCode(data[i]));
            }
        });
        res.on('end', () => {
            var ret = resp.join('');
            console.log(ret);
            var po = JSON.parse(ret);
            if(!po.successful){
                console.log("Illegal access server");
                process.exit(1);
            }
            cfg.serverKey = po.serverKey;
            callback();
            setInterval(ackOnTarantula,cfg.tarantula.ackTimeout);
        });    
    });
    req.on('error', (e) => {
        console.log("Error->"+e.message);
    });
    req.write(data);
    req.end();
}
function ackOnTarantula(){
    const headers = {Accept:'application/json','Content-type':'application/x-www-form-urlencoded','Tarantula-access-key':wcc.accessKey,'Tarantula-server-id':serverId,'Tarantula-action':'onAck'};
    const optsx ={rejectUnauthorized: false,secureProtocol: "TLSv1_2_method",hostname:wcc.host,port:wcc.port,path:'/'+wcc.path,method:'GET',headers:headers};
    const req = http.request(optsx,(res)=>{
        var resp=[];
        res.on('data', (data) => {
            for(var i = 0; i < data.length; ++i){
                resp.push(String.fromCharCode(data[i]));
            }
        });
        res.on('end', () => {
            var ret = resp.join('');
            console.log(ret);
        });    
    });
    req.on('error', (e) => {
        console.log("Error->"+e.message);
    });
    req.end();
}
function stopOnTarantula(callback){
    const headers = {Accept:'application/json','Content-type':'application/x-www-form-urlencoded','Tarantula-access-key':wcc.accessKey,'Tarantula-server-id':serverId,'Tarantula-action':'onStop'};
    const optsx ={rejectUnauthorized: false,secureProtocol: "TLSv1_2_method",hostname:wcc.host,port:wcc.port,path:'/'+wcc.path,method:'GET',headers:headers};
    const req = http.request(optsx,(res)=>{
        var resp=[];
        res.on('data', (data) => {
            for(var i = 0; i < data.length; ++i){
                resp.push(String.fromCharCode(data[i]));
            }
        });
        res.on('end', () => {
            var ret = resp.join('');
            console.log(ret);
            callback();
        });    
    });
    req.on('error', (e) => {
        console.log("Error->"+e.message);
    });
    req.end();
}
function actionOnTarantula(conn,_payload){
    _payload.token = conn.precence.token;
    _payload.clientId = conn.clientId;
    conn.streaming = _payload.streaming;
}
function validateOnTarantula(url,callback){
    var _payload = querystring.parse(url.substring(url.indexOf('?')+1));
    _payload.stub = _payload.stub/1;
    const data = JSON.stringify(_payload);
    const headers = {Accept:'application/json','Content-type':'application/x-www-form-urlencoded','Content-length':data.length,'Tarantula-magic-key':_payload.systemId,'Tarantula-tag':'index/user','Tarantula-action':'onTicket'};
    const optsx ={rejectUnauthorized: false,secureProtocol: "TLSv1_2_method",
        hostname:wcc.host,port:wcc.port,
        path:'/user/action',method:'POST',
        headers:headers};
    const req = http.request(optsx,(res)=>{
        var resp=[];
        res.on('data', (data) => {
            for(var i = 0; i < data.length; ++i){
                resp.push(String.fromCharCode(data[i]));
            }
        });
        res.on('end', () => {
            callback(fromString(resp.join('')));
        });
    });
    req.on('error', (e) => {
        console.log(e.message);
    });
    req.write(data);
    req.end();
}
function fromString(jstr){
    return JSON.parse(jstr);
}

