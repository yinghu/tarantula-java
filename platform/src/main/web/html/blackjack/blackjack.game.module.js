var BlackJackGame = (function(){
    
    let sw;
    let game;
    let _start = function(c,u,s){
        sw = c;
        TARA_API.onMessage('blackjack',u);
        let req = {action:'onStream',applicationId:game.applicationId,instanceId:game.instanceId,streaming:true,path:'/application/instance',data:{command:'onStream'}};
        console.log(req);
        TARA_API.send(req);
        s(game);
    };
    var _cashin = function(callback){
        var _payload = {serviceTag:"presence",command:"onTransfer",applicationId:game.applicationId,instanceId:game.instanceId,balance:game.entryCost};
        TARA_API.onService(_payload,callback);    
    };
    let _ping = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onPing"};
        TARA_API.onInstance(_payload,callback);
    };
    let _seat = function(sn,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onSeat",seatNumber:sn};
        TARA_API.onInstance(_payload,callback);
    };
    let _dealer_seat = function(sn,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onDealerSeat",seatNumber:sn};
        TARA_API.onInstance(_payload,callback);
    };
    let _off_dealer_seat = function(sn,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"offDealerSeat",seatNumber:sn};
        TARA_API.onInstance(_payload,callback);
    };
    let _wager = function(wg,stub,x,y,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onWager",balance:wg,stub:stub,x:x,y:y};
        TARA_API.onInstance(_payload,callback);
    };
    let _deal = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onDeal"};
        TARA_API.onInstance(_payload,callback);
    };
    let _stand = function(stub,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onStand",stub:stub};
        TARA_API.onInstance(_payload,callback);
    };
    let _hit = function(stub,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onHit",stub:stub};
        TARA_API.onInstance(_payload,callback);
    };
    let _double_down = function(stub,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onDoubleDown",stub:stub};
        TARA_API.onInstance(_payload,callback);
    };
    let _split = function(stub,callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onSplit",stub:stub};
        TARA_API.onInstance(_payload,callback);
    };
    let _soft17 = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onSoft17"};
        TARA_API.onInstance(_payload,callback);
    };
    let _shuffle = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onShuffle"};
        TARA_API.onInstance(_payload,callback);
    };
    let _face_up = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onFaceUp"};
        TARA_API.onInstance(_payload,callback);
    };
    let _payout = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onPayout"};
        TARA_API.onInstance(_payload,callback);
    };
    let _leave = function(callback){
        let _payload = {applicationId:game.applicationId,instanceId:game.instanceId,command:"onLeave"};
        TARA_API.onInstance(_payload,callback);
    };
    let _setup = function(setup){
        game = setup.game
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
        
        //black jack functions
        
        cashin : _cashin,
        ping : _ping,
        seat : _seat,
        dealerSeat : _dealer_seat,
        offDealerSeat : _off_dealer_seat,
        wager : _wager,
        deal : _deal,
        stand: _stand,
        hit: _hit,
        dowbleDown : _double_down,
        split: _split,
        soft17 : _soft17,
        shuffle : _shuffle,
        faceUp : _face_up,
        payout : _payout,
        leave : _leave, 
        
    }; 
}());