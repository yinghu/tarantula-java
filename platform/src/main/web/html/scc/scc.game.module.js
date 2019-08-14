var ShipCaptainCrewGame = (function(){
    
    let sw;
    let game;
    let _start = function(c,u,s){
        sw = c;
        TARA_API.onMessage('scc',u);
        let req = {action:'onStream',tag:game.tag,streaming:true,path:'/service/action',data:{command:'onStream'}};
        TARA_API.send(req);
        s(game);
    };
    let _pick = (ix,cback)=>{
        var _payload = {serviceTag:game.tag,command:'onPick',stub:ix};
        TARA_API.onService(_payload,cback);  
    };
    let _join = (gid,cback)=>{
        var _payload = {serviceTag:game.tag,command:'onJoin',accessId:gid};
        TARA_API.onService(_payload,cback);  
    };
    let _leave = (cback)=>{
        var _payload = {serviceTag:game.tag,command:'onLeave'};
        TARA_API.onService(_payload,cback);  
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
        pick : _pick,
        join : _join,
        leave : _leave,
    }; 

}());