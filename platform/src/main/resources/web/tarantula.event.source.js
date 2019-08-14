var ev;
self.onmessage = function(e){
    var sd = e.data;
    if(sd.cmd == 'start'){
        ev = new EventSource(sd.url);    
        if(sd.tag){
            ev.addEventListener(sd.tag,function(ex){
                self.postMessage({tag:ex.type,payload:JSON.parse(ex.data)});
            },false);
        }else{
            ev.onmessage = function(ex){
                self.postMessage({tag:ex.type,payload:JSON.parse(ex.data)});
            };
        }
    }
    else if(sd.cmd == 'register'){
        ev.addEventListener(sd.tag,function(ex){
            self.postMessage({tag:ex.type,payload:JSON.parse(ex.data)});
        },false);
    }
    else if(sd.cmd == 'end'){
        ev.close();
        self.close();
    }
}