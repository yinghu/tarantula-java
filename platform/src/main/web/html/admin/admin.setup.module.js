var AdminSetup= (function(){
    
    let sw;
    let adminObject;
    let _start = function(c,u,s,n){
        sw = c;
        TARA_API.onMessage(adminObject.label,u);
        let req = {action:'onStream',tag:adminObject.tag,streaming:true,path:'/service/action',data:{command:'onStream'}};
        TARA_API.send(req);
        s(adminObject);  
        TARA_API.onMessage('presence/notice',n);
        TARA_API.send({action:'onStart',streaming:true,label:'presence/notice',data:{command:'onStart'}});
    };
    let _add_lobby = function(fin,fout){
        let _payload = {serviceTag:adminObject.tag,command:"addLobby"};
        fin(_payload);
        TARA_API.onService(_payload,fout);
    };
    let _add_app = function(fin,fout){
        let _payload = {serviceTag:adminObject.tag,command:"addApplication"};
        fin(_payload);
        TARA_API.onService(_payload,fout);
    };
    let _enable_app = function(appid,out){
        let _payload = {serviceTag:adminObject.tag,command:"enableApplication",applicationId:appid};
        TARA_API.onService(_payload,out);
    };
    let _disable_app = function(appid,out){
        let _payload = {serviceTag:adminObject.tag,command:"disableApplication",applicationId:appid};
        TARA_API.onService(_payload,out);
    };
    let _launch = function(typeId,out){
        let _payload = {serviceTag:adminObject.tag,command:"onLaunch",typeId:typeId};
        TARA_API.onService(_payload,out);
    };
    let _shutdown = function(typeId,out){
        let _payload = {serviceTag:adminObject.tag,command:"onShutdown",typeId:typeId};
        TARA_API.onService(_payload,out);
    };
    let _reset = function(appId,out){
        let _payload = {serviceTag:adminObject.tag,command:"onReset",applicationId:appId};
        TARA_API.onService(_payload,out);
    };
    let _list_app = function(lobbyId,out){
        let _payload = {serviceTag:adminObject.tag,command:"applicationList",accessId:lobbyId};
        TARA_API.onService(_payload,out);
    };
    let _list_lobby = function(out){
        let _payload = {serviceTag:adminObject.tag,command:"lobbyList"};
        TARA_API.onService(_payload,out);
    };
    let _leave = function(){
        TARA_API.send({action:'onStop',streaming:true,label:'presence/notice',data:{command:'onStop'}});
        let _payload = {serviceTag:adminObject.tag,command:"onLeave"};
        TARA_API.onService(_payload,function(resp){
            currentModule = null;
        });
    };   
    let _setup = function(setup){
        console.log(setup);
        adminObject = setup.game;
    };
    let _swap = function(){
        if(sw){
            sw();
        }
    };
    return{
        setup : _setup,
        swap : _swap,    
        start : _start,
        leave : _leave,
        addLobby : _add_lobby,
        addApplication : _add_app,
        enableApplication : _enable_app,
        disableApplication : _disable_app,
        launch : _launch,
        reset : _reset,
        shutdown : _shutdown,
        applicationList : _list_app,
        lobbyList : _list_lobby,
    }; 

}());