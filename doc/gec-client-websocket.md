### CLIENT WEB SOCKET JSON API LIST
### W3C WEB SOCKET CALLBACK HOOK
```
    1. WEB SOCKET RETURN PAYLOAD FORMAT [LABEL]{THE JSON PAYLOAD}.
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
    4. SEND MESSAGE TO SERVER VIA WEBSOCKET
```
### Notification
### STREAMING CALLBACK
