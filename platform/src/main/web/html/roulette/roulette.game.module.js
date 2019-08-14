
var RouletteGame = (function(){
    
    let sw;
    let game;
    let _start = function(c,u,s){
        sw = c;
        TARA_API.onMessage('roulette',u);
        let req = {action:'onStream',applicationId:game.applicationId,instanceId:game.instanceId,streaming:true,path:'/application/instance',data:{command:'onStream'}};
        TARA_API.send(req);
        s(game);
    };
    var _cashin = function(callback){
        var _payload = {serviceTag:"presence",command:"onTransfer",applicationId:game.applicationId,instanceId:game.instanceId,balance:game.entryCost};
        TARA_API.onService(_payload,callback);    
    };
    let _dealerOnSeat = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onDealer"};
       TARA_API.onInstance(_payload,callback);
    };
    let _dealerOffSeat = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"offDealer"};
       TARA_API.onInstance(_payload,callback);
    };
    let _wheel = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onWheel"};
       TARA_API.onInstance(_payload,callback);
    };
    let _payout = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onPayout"};
       TARA_API.onInstance(_payload,callback);
    };
    let _wager = function(lineId,wg,index,callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onWager",stub:lineId,balance:wg,index:index};
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
        cashin : _cashin,
        leave : _leave,
        onDealer : _dealerOnSeat,
        offDealer : _dealerOffSeat,
        wheel : _wheel,
        wager : _wager,
        payout: _payout,
    }; 
}());