var SicBoGame = (function(){
    
    let sw;
    let game;
    let _start = function(c,u,s,n){
        sw = c;
        TARA_API.onMessage('sicbo',u);
        let req = {action:'onStream',applicationId:game.applicationId,instanceId:game.instanceId,streaming:true,path:'/application/instance',data:{command:'onStream'}};
        TARA_API.send(req);
        s(game);  
        TARA_API.onMessage('presence/notice',n);
        TARA_API.send({action:'onStart',streaming:true,label:'presence/notice',data:{command:'onStart'}});
    };
    let _query = function(){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onQuery"};
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    }; 
    let _backup = function(){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onBackup"};
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    };
    let _add_lobby = function(){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"addLobby"};
        _payload.typeId = "demo-sync";
        _payload.subtypeId ="demo-sync-lobby";
        _payload.type="lobby";
        _payload.category="game";
        _payload.tag="demo-sync";
        _payload.responseLabel = "demo";
        _payload.icon="html/blackjack/blackjack_icon.png";
        _payload.accessMode=12;
        _payload.deployCode=1;
        _payload.deployVersion=1;
        _payload.viewId = "game.lobby";
        _payload.name="Demo Sync";
        _payload.description="Application Module Demo";
        //_payload.configurationName="demo-sync";
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    };
    let _add_app = function(){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"addApplication"};
        _payload.typeId = 'demo-sync';
        _payload.subtypeId='demo-sync-a';
        _payload.type='application';
        _payload.category='casino';
        _payload.deployPriority=10;
        _payload.deployVersion=1;
        _payload.viewId = 'demo.sync.game';     
        _payload.name='Demo Sync';
        _payload.description='Applicaton Module Game';     
        _payload.codebase='file:///development/boost/target';
        _payload.moduleArtifact='tarantula-boost';
        _payload.moduleVersion='1.1';
        _payload.moduleName='com.tarantula.boost.Demo';      
        _payload.capacity=10;
        _payload.entryCost=5000; 
        _payload.maxInstancesPerPartition = 10;
        _payload.instancesOnStartupPerPartition =1;
        _payload.maxIdlesOnInstance =3;
        _payload.runtimeDuration=10;
        _payload.runtimeDurationOnInstance=1;
        //_payload.configurationType="v1";
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    };
    let _enable_app = function(appid){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"enableApplication",accessId:appid};
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    };
    let _disable_app = function(appid){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"disableApplication",accessId:appid};
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    };
    let _launch = function(){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onLaunch"};
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    };
    let _shutdown = function(){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onShutdown"};
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    };
    let _reset = function(){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onReset"};
        TARA_API.onInstance(_payload,function(resp){
            console.log(resp);
        });
    };
    let _leave = function(){
        TARA_API.send({action:'onStop',streaming:true,label:'presence/notice',data:{command:'onStop'}});
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onLeave"};
        TARA_API.onInstance(_payload,function(resp){
            currentModule = null;
        });
    };   
    let _setup = function(setup){
        game = setup.game;
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