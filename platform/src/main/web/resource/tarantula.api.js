var TARA_API = (function(){
        
  const vmap = new Map();    
  let presence = {};    
  let qdata ={};
  let lobbyList =[];
  let wsWorker;
    
  let _parse = function(data){
    data.lobbyList.forEach(function(v){
        lobbyList.push(v);
    });
  };            
  let _toWebSocketUrl = function(connection){       
    //use server connection config for web socket
    console.log(connection);
    return connection.protocol+'://'+connection.host+':'+connection.port+'/'+connection.path+'?connectionId='+connection.connectionId+'&accessKey='+connection.ticket+'&stub='+qdata.stub+'&systemId='+qdata.login;
  };
  let _connect = function(messageListener){
    if(qdata.offline){
        messageListener({label:'ws-offline',payload:{message:'offline play mode'}});
        return;
    }  
    let _url = _toWebSocketUrl(qdata.connection);
    wsWorker = new Worker('/resource/tarantula.web.socket.source.js');
    wsWorker.onmessage =(e)=>{
        messageListener(e.data);
    };
    wsWorker.postMessage({cmd:'start',url:_url,protocol:'tarantula-service'});
  };
  let _send = function(message){
    if(qdata.offline){
        return;
    }
    wsWorker.postMessage({cmd:'send',data:message});
  };
  let _disconnect = function(){
    if(qdata.offline){
        return;
    }
    wsWorker.postMessage({cmd:'close'});
  };

  let _lobbyList = function(){
      return lobbyList;
  };   
  let _query = function(){
      return qdata;
  };
  let _resetView = function(viewId){
    if(vmap.has(viewId)){
        vmap.delete(viewId);
    }
  };
  let _view = function(viewId,callback){
    qdata.viewId = viewId;
    let v = vmap.get(viewId);
    if(v==null){  
        let aj = new XMLHttpRequest();   
        aj.responseType = 'text';
        aj.onreadystatechange = function(){
            if(aj.status === 200 && aj.readyState === 4){
                let vp = JSON.parse(aj.responseText);
                vmap.set(viewId,vp);
                callback(vp);
            }
        };
        aj.open("GET","/view",true);
        aj.setRequestHeader('Accept','application/json');
        aj.setRequestHeader('Tarantula-view-id',viewId);
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
    if(presence.token){
        aj.setRequestHeader('Tarantula-token',presence.token);
    }
    aj.send();    
  };    
    
  let _index = function(){
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let jsb = JSON.parse(aj.responseText);
            //console.log(jsb);
            qdata.googleClientId = jsb.googleClientId;
            qdata.stripeClientId = jsb.stripeClientId;
            qdata.roleList = jsb.roleList;
            _parse(jsb);
        }
    };
    aj.open("GET","/user/action",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader('Tarantula-tag','index/user');
    aj.setRequestHeader('Tarantula-action','onIndex');
    aj.send();               
  };       
  let _getJson = (serviceTag,command,key,callback)=>{
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            callback(JSON.parse(aj.responseText));
        }
    };
    aj.open("GET","/service/action",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    aj.setRequestHeader('Tarantula-tag',serviceTag);
    aj.setRequestHeader('Tarantula-token',presence.token);
    aj.setRequestHeader('Tarantula-action',command);
    aj.setRequestHeader('Tarantula-name',key);
    aj.send();
  };
  let _upload = function(payload,fname,ctype,callback){
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
    aj.setRequestHeader("Content-type", ctype);//"application/java-archive"
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
            if(jsn.successful){
                presence = jsn.presence;
                qdata.systemId = presence.systemId;
                qdata.token = presence.token;
                qdata.stub = presence.stub;
                qdata.login = presence.login;
                _parse(jsn);
                callback({successful:true});                          
            }
            else{
                callback(jsn);
            }
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
  let _token = function(payload,callback){
      let _ps = JSON.stringify(payload);
      let aj = new XMLHttpRequest();
      aj.responseType = 'text';
      aj.onreadystatechange = function(){
          if(aj.status === 200 && aj.readyState === 4){
              let jsn = JSON.parse(aj.responseText);
              if(jsn.successful){
                presence = jsn.presence;
                qdata.systemId = presence.systemId;
                qdata.token = presence.token;
                qdata.stub = presence.stub;
                qdata.login = presence.login;
                _parse(jsn);
                callback({successful:true});                          
              }
              else{
                callback(jsn);
              }
          }
      };
      aj.open("POST","/user/action",true);
      aj.setRequestHeader('Accept','application/json');
      aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      aj.setRequestHeader('Tarantula-tag','index/user');
      aj.setRequestHeader('Tarantula-magic-key',payload.login);
      aj.setRequestHeader('Tarantula-action','onToken');
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
                qdata.stub = presence.stub;
                qdata.login = presence.login;
                _parse(p);
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
  let _resetCode = function(emailAddress,callback){
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let jsb = JSON.parse(aj.responseText);
            callback(jsb);
        }
    };
    aj.open("GET","/user/action",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader('Tarantula-tag','index/user');
    aj.setRequestHeader('Tarantula-action','onResetCode');
    aj.setRequestHeader('Tarantula-name',emailAddress);
    aj.send();               
  }; 
  let _resetPassword = function(payload,callback){
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
                qdata.stub = presence.stub;
                qdata.login = presence.login;
                _parse(p);
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
    aj.setRequestHeader('Tarantula-action','onResetPassword');
    aj.setRequestHeader('Tarantula-payload-size',_ps.length);  
    aj.send(_ps);                
  };
  let _setJson = function(serviceTag,command,key,payload,callback){
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
    aj.setRequestHeader('Tarantula-tag',serviceTag);
    aj.setRequestHeader('Tarantula-token',presence.token);
    aj.setRequestHeader('Tarantula-action',command);
    aj.setRequestHeader('Tarantula-name',key);
    aj.setRequestHeader('Tarantula-payload-size',_jp.length);  
    aj.send(_jp);
  }; 

  let _service = function(post,payload,callback){
    let _jp = JSON.stringify(payload);
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = function(){
        if(aj.status === 200 && aj.readyState === 4){
            let jsn = JSON.parse(aj.responseText);
            callback(jsn);
        }
    };
    aj.open(post?"POST":"GET","/service/action",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    aj.setRequestHeader('Tarantula-tag',payload.serviceTag);
    aj.setRequestHeader('Tarantula-token',presence.token);
    aj.setRequestHeader('Tarantula-action',payload.command);
    if(post){
        aj.setRequestHeader('Tarantula-payload-size',_jp.length);
    }  
    post?aj.send(_jp):aj.send();
  }; 
  let _logout = function(callback){   
    let payload = {serviceTag:'presence/lobby',command:'onAbsence',post:false};
    _service(false,payload,function(resp){
        presence ={};
        qdata ={};
        lobbyList.pop();
        vmap.clear();
        callback(resp);
    });              
  };
  let _play = (payload,callback)=>{
    //let _jp = JSON.stringify(payload);
    let aj = new XMLHttpRequest();   
    aj.responseType = 'text';
    aj.onreadystatechange = ()=>{
        if(aj.status === 200 && aj.readyState === 4){
            let resp = JSON.parse(aj.responseText);
            if(resp.successful){
                qdata.stub = resp.stub;
                qdata.offline = resp.offline;
                qdata.tournamentEnabled = resp.tournamentEnabled;
                if(!resp.offline){
                    let conn = resp.connection;
                    conn.protocol = conn.secured?'wss':'ws';
                    conn.path= payload.gamePath;
                    conn.ticket = resp.ticket;
                    qdata.connection = conn;
                }        
            }
            callback(resp);
        }
    };
    aj.open("GET","/service/action",true);
    aj.setRequestHeader('Accept','application/json');
    aj.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    aj.setRequestHeader('Tarantula-tag',payload.serviceTag);
    aj.setRequestHeader('Tarantula-token',presence.token);
    aj.setRequestHeader('Tarantula-action','onPlay');
    if(payload.tournamentEnabled){
        aj.setRequestHeader('Tarantula-instance-id',payload.tournamentId);
    }  
    aj.send();
  };
  //export APIs     
  return {
      query : _query,
      onIndex : _index, 
      onLobby : _lobbyList, 
      onView : _view,
      resetView : _resetView,
      onResource : _resource,
      onUpload : _upload,
      onRegister : _subscribe,
      onLogin : _login,
      onResetCode : _resetCode,
      onToken : _token,
      onResetPassword : _resetPassword,
      onLogout : _logout,
      onService : _service,
      onGet : _getJson,
      onSet : _setJson,
      connect : _connect,
      send : _send,
      disconnect : _disconnect,
      onPlay : _play
  };
    
})();