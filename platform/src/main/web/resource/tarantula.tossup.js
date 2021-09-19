class TossUp{
    
    constructor(){   
    }
    commit_config(){
        TARA_API.onGet('tossup/cfg','onList','category/abilities',(resp)=>{
            console.log(resp);
        });
    }
    commit_rating(callback) {
        let payload ={rating:{rank:1,delta:70},stats:[{name:'kills',value:1}]}; 
        TARA_API.onSet(TARA_API.query().stub.tag,'onUpdate','rating',payload,(resp)=>{
            callback(resp);    
        });
    }
    commit_leave(callback){
        let cmd ={serviceTag:TARA_API.query().stub.tag,command:'onLeave'};
        TARA_API.onService(false,cmd,(resp)=>{
            TARA_API.disconnect();
            TGL.shutdown();
            callback(resp);
        });
    }
    commit_kills(callback){
        let payload ={rating:{rank:0,delta:10},stats:[{name:'kills',value:1}]}; 
        TARA_API.onSet(TARA_API.query().stub.tag,'onUpdate','stats',payload,(resp)=>{
            callback(resp);    
        });    
    }
    commit_wins(callback){
        let payload ={rating:{rank:0,delta:10},stats:[{name:'wins',value:1}]}; 
        TARA_API.onSet(TARA_API.query().stub.tag,'onUpdate','stats',payload,(resp)=>{
            callback(resp);    
        });    
    }
    commit_score(callback){
        let payload ={rating:{rank:0,delta:10},stats:[{name:'wins',value:1}],tournament:{score:100}}; 
        TARA_API.onSet(TARA_API.query().stub.tag,'onUpdate','tournament',payload,(resp)=>{
            callback(resp);    
        });    
    }
    commit_list(callback){
        TARA_API.onGet(TARA_API.query().stub.tag,'onList','tournament',(resp)=>{
            callback(resp);    
        });  
    }
}