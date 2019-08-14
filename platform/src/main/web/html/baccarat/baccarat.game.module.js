
var BaccaratGame = (function(){
    
    let sw;
    let game;
    let _start = function(c,u,s){
        sw = c;
        TARA_API.onMessage('baccarat',u);
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
    let _deal = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onDeal"};
       TARA_API.onInstance(_payload,callback);
    };
    let _hitOnPlayer = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onPlayer"};
       TARA_API.onInstance(_payload,callback);
    };
    let _hitOnBanker = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onBanker"};
       TARA_API.onInstance(_payload,callback);
    };
    let _payout = function(callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onPayout"};
       TARA_API.onInstance(_payload,callback);
    };
    let _wager = function(lineId,wg,x,y,ix,callback){
       let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onWager",stub:lineId,balance:wg,x:x,y:y,index:ix};
       TARA_API.onInstance(_payload,callback);
    };
    let _leave = function(){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onLeave"};
        TARA_API.onInstance(_payload,function(resp){
            currentModule = null;
        });
    };
    let _ping = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onPing"};
        TARA_API.onInstance(_payload,callback);
    };
    let _seat = function(sn,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onSeat",stub:sn};
        TARA_API.onInstance(_payload,callback);
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
        onDealerSeat : _dealerOnSeat,
        offDealerSeat : _dealerOffSeat,
        deal : _deal,
        payout : _payout,
        onPlayer : _hitOnPlayer,
        onBanker : _hitOnBanker,
        seat : _seat,
        wager: _wager,
        ping : _ping,
        leave : _leave,
    }; 
}());