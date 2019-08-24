var DemoSyncGame = (function(){
    let sw,game;
    let _start = function(swap,onMessage,setup,onNotification){
        sw = swap;
        TARA_API.onMessage(game.label,onMessage);
        let req = {action:'onStream',applicationId:game.applicationId,instanceId:game.instanceId,streaming:true,path:'/application/instance',data:{command:'onStream'}};
        TARA_API.send(req); 
        TARA_API.onMessage('presence/notice',onNotification);
        TARA_API.send({action:'onStart',streaming:true,label:'presence/notice',data:{command:'onStart'}});
        setup(game);
    };
    let _setup = function(setup){
        game = setup.game;
        console.log(game);
    };
    let _swap = function(){
        if(sw){
            sw();
        }
    };

    let _leave = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:'onLeave'};
        TARA_API.onInstance(_payload,callback);
    };
    let _sit = function(cmd,callback){
        let _payload = {action:cmd,applicationId:game.applicationId,instanceId:game.instanceId,streaming:false,path:'/application/instance',data:{command:cmd}};
        TARA_API.send(_payload);
        //TARA_API.onInstance(_payload,callback);
    };
    var _item = function(callback){
        var _payload = {serviceTag:"presence/lobby",command:"onTransfer",applicationId:game.applicationId,instanceId:game.instanceId,balance:5000};
        TARA_API.onService(_payload,callback);    
    };
    let _item1 = function(out){
        let _payload = {serviceTag:'leaderboard/top10',header:'presence',category:'LoginCount',classifier:'T'};
        TARA_API.onService(_payload,out);
    };  
    return{
        setup : _setup,
        swap : _swap,
        start : _start,
        
        leave : _leave,
        sit : _sit,
        item: _item,
    };

}());