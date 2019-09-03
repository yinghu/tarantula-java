/**
    TARANTULA PLATFORM NODE JS WEBSOCKET DAEMON
**/
const WebSocketServer = require('websocket').server;
const fs = require('fs');
var  cfg = JSON.parse(fs.readFileSync('ws.config','utf-8'));
var secured = cfg.front.secured;
const https = secured?require('https'):require('http');
const http = require('http');
const net = require('net');
const querystring = require('querystring');
const uuidv1 = require('uuid/v1');
const cMap = new Map(); //web socket client mapping clientId => connection
const pMap = new Map(); //server push event mapping label ==> updated payload
var mresult = {payload:[],cid:[],lbl:[],pos:0};
var mlistener = connectOnTarantula();
var connected = false;
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
                        if(!pMap.has(_pr.label)){
                           pMap.set(_pr.label,{payload:'{}',listeners:[]});
                        }
                        let pse = pMap.get(_pr.label);
                        connection.sendUTF(pse.payload);
                        if(_pr.streaming){
                            //register callback if streaming required otherwise send back per request
                            if(_pr.action === 'onStart'){
                                console.log('register on ['+_pr.label+'] from ['+connection.clientId+']');
                                pse.listeners.push(connection.clientId);
                            }
                            else if(_pr.action ==='onStop'){
                                let ix = pse.listeners.indexOf(connection.clientId);
                                if(ix>=0){
                                    console.log('unregister on ['+_pr.label+'] from ['+connection.clientId+'/'+ix+']');
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
                console.log('Error on web socket connection');
                console.log(err);
                cMap.delete(connection.clientId);
            });
            connection.on('close', function(reasonCode, description) {
                cMap.delete(connection.clientId);
                console.log('Peer closed from /'+reasonCode+"/"+description+"/"+ connection.remoteAddress +'/'+connection.clientId);
            });
        }
        else{
            console.log('ticket failed to validate');
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
    wsServer.shutDown();
    mlistener.end();
    web.close(()=>{
        console.log("web socket is closed");
    });
};
function connectOnTarantula(){
    var soc = net.createConnection(cfg.server.connection.port,cfg.server.connection.host,()=>{
        console.log("message listener connected to ["+cfg.server.connection.host+":"+cfg.server.connection.port+"]");
        //write one way protocol register to server
        let req = {action:'onConnect',clientId:'push/streaming',path:'/push/action',streaming:true,serverId:serverId,data:{command:'onConnect',type:cfg.front.type,secured:cfg.front.secured,protocol:secured?'wss':'ws',host:cfg.front.host,port:cfg.front.port,path:cfg.front.path,serverId:serverId}};
        soc.write(toJSONString(req));
        connected = true;
    });
    soc.on('data',(data)=>{
        for(var i = 0; i < data.length; ++i){
            var c = String.fromCharCode(data[i]);
            if(c==='|'){
               var cid = mresult.cid.join('');
               var lbl = mresult.lbl.join('');
               var mx = mresult.payload.join('');
               //console.log(cid);
               //console.log(lbl);
               //console.log(mx);
               var conn = cMap.get(cid);
               if(conn){
                    try{
                        conn.sendUTF(mx);
                    }catch(er){
                        console.log('client removed->'+cid);
                        cMap.delete(cid);
                    }
               }
               else{//server push event
                    //console.log(mx);
                    if(!pMap.has(lbl)){
                        pMap.set(lbl,{payload:'{}',listeners:[]});
                    }
                    let pse = pMap.get(lbl);
                    pse.payload = mx;
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
       connected = false;
    });
    soc.on('close',()=>{
      console.log('trying to connect on server');
      if(!connected){
          setTimeout(()=>{
              mlistener = connectOnTarantula();
          },5000);
      }
    });
    return soc;
}
function actionOnTarantula(conn,_payload){
    _payload.token = conn.precence.token;
    _payload.clientId = conn.clientId;
    conn.streaming = _payload.streaming;
    mlistener.write(toJSONString(_payload));
}
function validateOnTarantula(url,callback){
    var _payload = querystring.parse(url.substring(url.indexOf('?')+1));
    const data = JSON.stringify(_payload);
    //const opts ={hostname:'realnumber.net',path:'/user/action',method:'POST',headers:{Accept:'application/json','Content-type':'application/x-www-form-urlencoded','Content-length':data.length,'Tarantula-magic-key':_payload.systemId,'Tarantula-tag':'user','Tarantula-action':'onTicket'}};
    const optsx ={hostname:cfg.server.web.host,port:cfg.server.web.port,path:'/user/action',method:'POST',headers:{Accept:'application/json','Content-type':'application/x-www-form-urlencoded','Content-length':data.length,'Tarantula-magic-key':_payload.systemId,'Tarantula-tag':'index/user','Tarantula-action':'onTicket'}};
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

