var CrapsGame = (function(){
    
    let sw;
    let game;
    let _start = function(c,u,s){
        sw = c;
        TARA_API.onMessage('craps',u);
        let req = {action:'onStream',applicationId:game.applicationId,instanceId:game.instanceId,streaming:true,path:'/application/instance',data:{command:'onStream'}};
        TARA_API.send(req);
        s(game);
    };
    var _cashin = function(callback){
        var _payload = {serviceTag:"presence",command:"onTransfer",applicationId:game.applicationId,instanceId:game.instanceId,balance:game.entryCost};
        TARA_API.onService(_payload,callback);    
    };
    let _roll = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onRoll"};
       TARA_API.onInstance(_payload,callback);
    };
    let _wager = function(lineId,wg,x,y,index,callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onWager",stub:lineId,balance:wg,x:x,y:y,index:index};
       TARA_API.onInstance(_payload,callback);
    };
    let _ondealer = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onDealer"};
       TARA_API.onInstance(_payload,callback);
    };
    let _offdealer = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"offDealer"};
       TARA_API.onInstance(_payload,callback);
    };
    let _leave = function(){
        //var req = {label:'game',action:'onStop',tag:'simulation',streaming:true,path:'/service/action',data:{target:'game'}};
        //TARA_API.send(req);
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
        cashin :_cashin,
        leave : _leave,
        roll : _roll,
        wager : _wager,
        onDealer : _ondealer,
        offDealer : _offdealer,
    }; 
}());