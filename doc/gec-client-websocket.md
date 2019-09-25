### CLIENT WEB SOCKET JSON API LIST
### W3C WEB SOCKET CALLBACK HOOK
```
    1. WEB SOCKET RETURN PAYLOAD FORMAT [LABEL]{JSON PAYLOAD}.
       SAMPLE : demo{"timer":{"h":0,"m":2,"s":24}}
    2. MESSAGE DISPATCH ON CALLBACK WITH LABEL  
    3. PROCESS PAYLOAD ON MESSAGE WEB SOCKET CALLBACK 
```
```C#
     ------ SAMPLE C# CODE ----- 
    wc.OnMessage += (sender,e) =>{
        if(e.IsText){
            int ix = e.Data.IndexOf("{");
            string lb = e.Data.Substring(0,ix);
            JSONObject payload = new JSONObject(e.Data.Substring(ix));        
            Action<JSONOBJECT> callback = callbackMapping[lb](payload);
        }    
    };
```
```
    4. CONNECT TO WEB SOCKET SERVER
       ws{s}://{host}:{port}/tarantula?accessKey={ticket}&stub={stub}&systemId={user_login};
       ------- REQUEST SUB PROTOCOL HEADER -------
       Sec-WebSocket-Protocol: tarantula-service
```
```C#
    string ws = "ws://localhost:8080/tarantula?accessKey=auth_ticket&stub=1&systemId=abc123";
    WebSocket wc = new WebSocket(ws,"tarantula-service");
    wc.connect();
```
```
    5. SEND MESSAGE TO SERVICE ON SERVER VIA WEBSOCKET 
    PAYLOAD : {action:"{command}",path:"/service/action",streaming:false,tag:{tag},data:{client payload}}
```
```
    6. SEND MESSAGE TO INSTANCE ON SERVER VIA WEBSOCKET 
    PAYLOAD : {action:"{command}",path:"/application/instance",streaming:false,applicationId:{applicationId},instanceId:{instanceId},data:{client payload}}
```
```
    7. REGISTER NOTIFICATION ON SERVER VIA WEBSOCKET 
    PAYLOAD : {action:"onStart",streaming:true,label:{notice/label},data:{}}    
```
    8. UNREGISTER NOTIFICATION ON SERVER VIA WEBSOCKET 
    PAYLOAD : {action:"onStop",data:{}}
```

