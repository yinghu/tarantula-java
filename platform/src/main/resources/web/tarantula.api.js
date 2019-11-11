var TARA_API = (function(){
        
  const amap = new Map(); 
  const vmap = new Map(); 
  const cMap = new Map();    
  let presence = {};    
  let qdata ={};
  let wsWorker ;
    
  let _parse = function(data,cb){
      data.lobbyList.forEach(function(v){
          amap.set(v.descriptor.typeId,v);
          v.applications.forEach(function(b){
            if(b.singleton){
                amap.set(b.tag,b);
            }
            else{
                amap.set(b.applicationId,b);
            }
           });
           cb(v);
      });
  };            
  let _toWebSocketUrl = function(){       
      //use server connection config for web socket
      console.log(qdata.connection);
      return qdata.connection.protocol+'://'+qdata.connection.host+':'+qdata.connection.port+'/'+qdata.connection.path+'?accessKey='+qdata.ticket+'&stub='+qdata.stub+'&systemId='+qdata.login;
  };
  let _onmessage = function(e){
      //dispatch data to callback registered by label
      if(e.data.label&&cMap.has(e.data.label)){
        var cb = cMap.get(e.data.label);
        cb.call(null,e.data.payload);
      }
  };
  let _addMessageListener = function (label,callback){
      if(cMap.has(label)){
        cMap.delete(label);
      }
      cMap.set(label,callback);
  };  
  let _sendMessage = function(msg){
      wsWorker.postMessage({cmd:'send',data:msg});
  };

  let _descriptor = function(dk){
      return amap.get(dk);
  };   
  let _query = function(){
      return qdata;
  };
  let _view = function(viewId,callback){
    let v = vmap.get(viewId);
    if(v==null){  
        let aj = new XMLHttpRequest();   
        aj.responseType = 'text';
        aj.onreadystatechange = function(){
            if(aj.status === 200 && aj.readyState === 4){
                let vp = JSON.parse(aj.responseText);
                vmap.set(viewId,vp.view);
                callback(vp.view);
            }
        };
        aj.open("GET","/user/view",true);
        aj.setRequestHeader('Accept','application/json');
        aj.setRequestHeader('View-id',viewId);
        aj.setRequestHeader('Tarantula-tag','index/lobby');
        aj.send();
    }
    else{
        callback(v);
    }  
  };
   
  let _resource = function(resource,callback){
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            callback(aj.responseText);
        }
    };
    if(resource.flag === undefined){
        aj.open("GET","/"+resource.name,true);
    }else{
        aj.open("GET","/"+resource.name+'?flag='+resource.flag,true);
    }
    aj.setRequestHeader('Accept',resource.type);
    aj.send();    
  };    
    
  let _index = function(callback){
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let jsb = JSON.parse(aj.responseText);
            _parse(jsb,callback);
        }
    };
    aj.open("GET","/user/index",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader('Tarantula-tag','index/lobby');
    aj.send();               
  };       

  let _upload = function(payload,fname,callback){
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let p = JSON.parse(aj.responseText);
            callback(p);
        }
        else{
            callback({successful:false,message:'Please waiting .....'});
        }
    };
    aj.open('POST','/upload/'+fname,true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader("Content-type", "application/java-archive");
    aj.setRequestHeader('Tarantula-token',presence.token);
    aj.send(payload);
  };
  let _subscribe = function(payload,callback){
    let _ps = JSON.stringify(payload);
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let jsn = JSON.parse(aj.responseText);
            callback(jsn);
        }
    };
    aj.open("POST","/user/action",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    aj.setRequestHeader('Tarantula-tag','index/user');
    aj.setRequestHeader('Tarantula-magic-key',payload.login);
    aj.setRequestHeader('Tarantula-action','onRegister');
    aj.setRequestHeader('Tarantula-payload-size',_ps.length);  
    aj.send(_ps);               
  };
  let _reset = function(payload,callback){
      let _ps = JSON.stringify(payload);
      let aj = new XMLHttpRequest();
      aj.responseType = 'text';
      aj.onreadystatechange = function(){
          if(aj.status === 200 && aj.readyState === 4){
              let jsn = JSON.parse(aj.responseText);
              callback(jsn);
          }
      };
      aj.open("POST","/user/action",true);
      aj.setRequestHeader('Accept','application/json');
      aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      aj.setRequestHeader('Tarantula-tag','index/user');
      aj.setRequestHeader('Tarantula-magic-key',payload.email);
      aj.setRequestHeader('Tarantula-action','onReset');
      aj.setRequestHeader('Tarantula-payload-size',_ps.length);
      aj.send(_ps);
  };
  let _login = function(payload,callback){
    let _ps = JSON.stringify(payload);
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let p = JSON.parse(aj.responseText);
            if(p.successful){
                presence = p.presence;
                qdata.systemId = presence.systemId;
                qdata.token = presence.token;
                qdata.ticket = presence.ticket;
                qdata.stub = presence.stub;
                qdata.login = presence.login;
                qdata.connection = p.connection;
                wsWorker = new Worker('/resource/tarantula.web.socket.source.js');//move to login
                wsWorker.onmessage = _onmessage;
                wsWorker.postMessage({cmd:'start',url:_toWebSocketUrl(),protocol:'tarantula-service'});
                _parse(p,function(v){});
                callback({successful:true});
            }else{
                callback(p);
            }
        }
    };
    aj.open("POST","/user/action",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    aj.setRequestHeader('Tarantula-tag','index/user');
    aj.setRequestHeader('Tarantula-magic-key',payload.login);
    aj.setRequestHeader('Tarantula-action','onLogin');
    aj.setRequestHeader('Tarantula-payload-size',_ps.length);  
    aj.send(_ps);                
  };
  let _presence = function(callback){   
    let payload = {serviceTag:'presence/lobby',command:'onPresence'};
    _service(payload,function(resp){
        qdata.balance = resp.presence.balance;
        //qdata.ticket = resp.presence.ticket;
        //console.log(resp.connection);
        //qdata.connection = resp.connection;
        //_parse(resp,function(v){});
        //wsWorker = new Worker('/resource/tarantula.web.socket.source.js');//move to login
        //wsWorker.onmessage = _onmessage;
        //wsWorker.postMessage({cmd:'start',url:_toWebSocketUrl(),protocol:'tarantula-service'});
        callback({successful:true});
    });               
  }; 
  let _connect = function(){
    let payload = {serviceTag:'presence/lobby',command:'onTicket'};
    _service(payload,function(resp){
        qdata.ticket = resp.presence.ticket;
        wsWorker.postMessage({cmd:'start',url:_toWebSocketUrl(),protocol:'tarantula-service'});
    });
  };
  let _profile = function(playerId,callback){
    let payload = {serviceTag:'presence/profile',command:'onProfile',headers:[{name:'systemId',value:playerId}]};
    _service(payload,function(resp){
        callback(resp);
    });
  };
  let _service = function(payload,callback){
    let _jp = JSON.stringify(payload);
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let jsn = JSON.parse(aj.responseText);
            callback(jsn);
        }
    };
    aj.open("POST","/service/action",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    aj.setRequestHeader('Tarantula-tag',payload.serviceTag);
    aj.setRequestHeader('Tarantula-token',presence.token);
    aj.setRequestHeader('Tarantula-action',payload.command);
    aj.setRequestHeader('Tarantula-payload-size',_jp.length);  
    aj.send(_jp);
  };
  let _instance = function(payload,callback){
    let _jp = JSON.stringify(payload);
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let jsn = JSON.parse(aj.responseText);
            callback(jsn);
        }
    };
    aj.open("POST","/application/instance",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    aj.setRequestHeader('Tarantula-tag',payload.serviceTag);
    aj.setRequestHeader('Tarantula-token',presence.token);
    aj.setRequestHeader('Tarantula-application-id',payload.applicationId);
    aj.setRequestHeader('Tarantula-instance-id',payload.instanceId);
    aj.setRequestHeader('Tarantula-action',payload.command);
    aj.setRequestHeader('Tarantula-payload-size',_jp.length);  
    aj.send(_jp);
  };     
  let _logout = function(callback){   
    let payload = {serviceTag:'presence/lobby',command:'onAbsence'};
    _service(payload,function(resp){
        //amap.clear();
        presence = null;
        qdata ={};
        wsWorker.postMessage({cmd:'close'});
        callback(resp);
    });              
  };
  //export APIs     
  return {
      query : _query,
      descriptor : _descriptor,
      onIndex : _index,  
      onView : _view,
      onResource : _resource,
      onUpload : _upload,
      onRegister : _subscribe,
      onLogin : _login,
      onReset : _reset,
      onPresence : _presence,
      onProfile : _profile,
      onService : _service,
      onInstance : _instance,
      onLogout : _logout,
      onMessage: _addMessageListener,
      send: _sendMessage,
      connect :_connect,
  };
    
})();