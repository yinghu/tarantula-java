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
var secured = cfg.front.secured;
if(fs.existsSync('/etc/tarantula/ip.txt')){
    cfg.front.host = fs.readFileSync('/etc/tarantula/ip.txt','utf-8');//replace ip from ip.txt
    console.log("replacing ip with ["+cfg.front.host+"]");
}
const https = secured?require('https'):require('http');
const http = require(cfg.server.web.type);//use http or https
const net = require('net');
const querystring = require('querystring');
const {v1: uuidv1} = require('uuid');
const cMap = new Map(); //web socket client mapping clientId => connection
const pMap = new Map(); //server push event mapping label ==> updated payload
var mresult = {payload:[],cid:[],lbl:[],pos:0};
var mlistener;
var web;
var serverId = uuidv1();
if(secured){
    const options = {
      key: fs.readFileSync('/etc/tarantula/private.pem'),
      cert: fs.readFileSync('/etc/tarantula/certificate.pem')
    };
    web = https.createServer(options);
}
else{
    web = https.createServer();
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
    //console.log("Origin->"+origin);
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
    web.listen(cfg.front.port,()=>{
        console.log("Web socket is started on ["+cfg.front.port+"] on TSL (HTTPS)["+secured+"]");
    });
};

exports.stop=()=>{
    console.log("closing web socket and service connection");
    wsServer.closeAllConnections();
    wsServer.shutDown();
    mlistener.end();
    process.exit(1);
    //web.close(()=>{
        //console.log("web socket is closed");
        //process.exit(1);
    //});
};
connectOnTarantula();
function connectOnTarantula(){
    return registerOnTarantula(()=>{
    var soc = net.createConnection(cfg.server.connection.port,cfg.server.connection.host,()=>{
        console.log("message listener connected to ["+cfg.server.connection.host+":"+cfg.server.connection.port+"]");
        //write one way protocol register to server
        let req = {action:'onConnect',clientId:'push/streaming',path:'/push/action',streaming:true,serverId:serverId,ticket:cfg.server.connection.ticket,
        data:{command:'onConnect',type:cfg.front.type,secured:cfg.front.secured,protocol:secured?'wss':'ws',host:cfg.front.host,port:cfg.front.port,path:cfg.front.path,serverId:serverId,maxConnections:cfg.front.maxConnections}};
        soc.write(toJSONString(req));
    });
    soc.on('data',(data)=>{
        for(var i = 0; i < data.length; ++i){
            var c = String.fromCharCode(data[i]);
            if(c==='|'){
               var cid = mresult.cid.join('');
               var lbl = mresult.lbl.join('');
               var mx = mresult.payload.join('');
               var conn = cMap.get(cid);
               if(conn){
                  conn.sendUTF(mx);
               }
               else{//server push event
                    //label format {label}#{gameId}?{command}
                    //console.log(lbl);
                    var _gameId = (lbl.indexOf("?")!=-1)?lbl.substring(lbl.indexOf("#")+1,lbl.indexOf("?")):lbl.substring(lbl.indexOf("#")+1);
                    //console.log(_gameId);
                    if(!pMap.has(_gameId)){
                        pMap.set(_gameId,{listeners:[]});
                    }
                    let pse = pMap.get(_gameId);
                    pse.listeners.forEach((c,ix)=>{
                        //synchronize client
                        let _sc = cMap.get(c);
                        if(_sc!=null){
                            _sc.sendUTF(mx);
                        }
                        else{
                            pse.listeners.splice(ix,1);
                        }
                        //remove clientId from index if connect lost
                    });
               }
               mresult.payload=[];
               mresult.cid=[];
               mresult.lbl=[];
               mresult.pos=0;
            }else{
               if(mresult.pos === 2){
                    mresult.payload.push(c);
               }
               else if(mresult.pos === 0){
                    if(c != ','){
                        mresult.cid.push(c);
                    }
                    else{
                        mresult.pos = 1;
                    }
               }
               else if(mresult.pos === 1){
                    if(c != '{'){
                        mresult.lbl.push(c);
                    }
                    else{
                        mresult.pos = 2;
                    }
                    mresult.payload.push(c);
               }
            }
        }
    });
    soc.on('error',(err)=>{
       console.log(err);
       console.log('trying to connect on server on error');
       connectOnTarantula();
    });
    soc.on('close',()=>{
        console.log('socket connection closed');
    });
    return soc;
    });
}
function registerOnTarantula(callback){
    const headers = {Accept:'application/json','Content-type':'application/x-www-form-urlencoded','Tarantula-access-key':cfg.server.web.key,'Tarantula-server-id':serverId,'Tarantula-action':'onTicket'};
    const optsx ={rejectUnauthorized: false,hostname:cfg.server.web.host,port:cfg.server.web.port,path:'/push',method:'GET',headers:headers};
    const req = http.request(optsx,(res)=>{
        var resp=[];
        res.on('data', (data) => {
            for(var i = 0; i < data.length; ++i){
                resp.push(String.fromCharCode(data[i]));
            }
        });
        res.on('end', () => {
            var ret = JSON.parse(resp.join(''));
            if(!ret.successful){
                console.log("Illegal access server");
                process.exit(1);
            }
            console.log(ret.host+":"+ret.port+"//"+ret.ticket);
            cfg.server.connection={port:ret.port,host:ret.host,ticket:ret.ticket};
            mlistener = callback();
        });    
    });
    req.on('error', (e) => {
        console.log("Retrying->"+e.message);
        connectOnTarantula();
    });
    req.end();
}
function actionOnTarantula(conn,_payload){
    _payload.token = conn.precence.token;
    _payload.clientId = conn.clientId;
    conn.streaming = _payload.streaming;
    mlistener.write(toJSONString(_payload));
}
function validateOnTarantula(url,callback){
    var _payload = querystring.parse(url.substring(url.indexOf('?')+1));
    _payload.stub = _payload.stub/1;
    const data = JSON.stringify(_payload);
    const headers = {Accept:'application/json','Content-type':'application/x-www-form-urlencoded','Content-length':data.length,'Tarantula-magic-key':_payload.systemId,'Tarantula-tag':'index/user','Tarantula-action':'onTicket'};
    const optsx ={rejectUnauthorized: false,secureProtocol: "TLSv1_2_method",
        hostname:cfg.server.web.host,port:cfg.server.web.port,
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
function toJSONString(json){
    return JSON.stringify(json)+"|";
}

