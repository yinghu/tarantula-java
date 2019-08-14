//Common Game Lobby Module
var GameLobby = (function(){
    
    let sw;
    let lobby;
    let _start = function(c,s,lb){
        sw = c;
        //TARA_API.onMessage('presence/lobby',function(m){
            //console.log(m);
        //});
        //let req = {action:'onStart',streaming:true,label:'presence/lobby',data:{command:'onStart'}};
        //TARA_API.send(req);
        let _payload ={serviceTag:lobby.tag,command:'onLobby',typeId:lobby.typeId};
        TARA_API.onService(_payload,function(resp){
            s(resp);        
        });
        lb(lobby);
    };
    
    let _exit = function(callback){
        TARA_API.onLogout(function(resp){
            currentModule = null;
            callback(resp);
        });
    };
    let _setup = function(setup){
        lobby = TARA_API.descriptor(setup.typeId).descriptor;
        //console.log(lobby);
        //typeId = lob.typeId;
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
        exit : _exit,
    }; 
}());