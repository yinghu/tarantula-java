var AdminDataStore = (function(){
    
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
    let _onAction = function(fin,out){
        let _payload = {serviceTag:adminObject.tag};
        fin(_payload);
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
        onAction : _onAction,
    }; 

}());