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
    let _query = function(){
        let _payload = {serviceTag:adminObject.tag,command:"onQuery"};
        TARA_API.onService(_payload,function(resp){
            console.log(resp);
        });
    }; 
    let _backup = function(){
        let _payload = {serviceTag:adminObject.tag,command:"onBackup"};
        TARA_API.onService(_payload,function(resp){
            console.log(resp);
        });
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
    let _enable_app = function(appid){
        let _payload = {serviceTag:adminObject.tag,command:"enableApplication",accessId:appid};
        TARA_API.onService(_payload,function(resp){
            console.log(resp);
        });
    };
    let _disable_app = function(appid){
        let _payload = {serviceTag:adminObject.tag,command:"disableApplication",accessId:appid};
        TARA_API.onService(_payload,function(resp){
            console.log(resp);
        });
    };
    let _launch = function(){
        let _payload = {serviceTag:adminObject.tag,command:"onLaunch"};
        TARA_API.onService(_payload,function(resp){
            console.log(resp);
        });
    };
    let _shutdown = function(){
        let _payload = {serviceTag:adminObject.tag,command:"onShutdown"};
        TARA_API.onService(_payload,function(resp){
            console.log(resp);
        });
    };
    let _reset = function(){
        let _payload = {serviceTag:adminObject.tag,command:"onReset"};
        TARA_API.onService(_payload,function(resp){
            console.log(resp);
        });
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
        query : _query,
        backup : _backup,
        addLobby : _add_lobby,
        addApplication : _add_app,
        enableApplication : _enable_app,
        disableApplication : _disable_app,
        launch : _launch,
        reset : _reset,
        shutdown : _shutdown,
    }; 

}());