var ws;
var ci;
self.onmessage = function(e){
    var sd = e.data;
    if(sd.cmd === 'send'){
        if(ws.readyState === 1){
            ws.send(JSON.stringify(sd.data));
        }
        else{
            self.postMessage({label:'ws',action:'error',payload:{code:300,message:'error on send'}});
        }
    }
    else if(sd.cmd === 'start'){
        ci = sd;
        ws = _connect();
    }
    else if(sd.cmd === 'close'){
        ws.close();
        self.close();
    }
}
function _connect(){
    let _ws = new WebSocket(ci.url,ci.protocol);
    _ws.onopen=function(){
        self.postMessage({label:'ws',action:'open',payload:{code:200,message:'web socket ready'}});
    };
    _ws.onmessage=function(e){
        //format [label]{json string}
        var ix = e.data.indexOf('{');
        var lbl = e.data.substring(0,ix);
        var jsm = JSON.parse(e.data.substring(ix));
        //console.log(lbl);
        //console.log(jsm);
        self.postMessage({label:lbl,payload:jsm});
    };
    _ws.onerror=function(e){
        self.postMessage({label:'ws',action:'error',payload:{code:300,message:'web socket error'}});
    };
    _ws.onclose=function(e){
        self.postMessage({label:'ws',action:'close',payload:{code:300,message:'web socket closed'}});
    };
    return _ws;
}