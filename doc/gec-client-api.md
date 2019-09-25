### CLIENT RESTFUL AJAX/JSON API LIST 
### Index AJAX GET /user/index
```
    -----REQUEST HTTP HEADERS----- 
    Accept: application/json
    Tarantula-tag: index/lobby
    
    ----RESPONSE HTTP HEADERS------
    Status Code: 200 OK
    Content-length: 2474
    Content-type: application/json
    
```
```JSON
    ------ SAMPLE RESPONSE CONTENT ----
{"command":"index","code":200,"timestamp":0,"sequence":0,"successful":true,
 "lobbyList":[
    {"descriptor":
        {"singleton":false,
         "deployCode":0,
         "type":"lobby",
         "typeId":"index",
         "category":"service",
         "capacity":0,
         "name":"Index",
         "description":"Tarantula Lobby Index",
         "applicationId":"BDS01/5d844d31e1b44e939724f4c426c6280e",
         "accessMode":10,
         "entryCost":0.0,
         "entryCostAsString":"0.00",
         "tournamentEnabled":false,
         "disabled":false,
         "resetEnabled":false,
         "timerOnModule":0,
         "runtimeDuration":0,
         "runtimeDurationOnInstance":0},
     "applications":[
        {"singleton":true,
         "deployCode":0,
         "tag":"index/user",
         "type":"application",
         "typeId":"index",
         "subtypeId":"index-user",
         "capacity":0,
         "name":"User",
         "description":"Tarantula User Management",
         "applicationId":"BDS01/42f2a72599224b11aa727f8aae119db0",
         "accessMode":10,
         "entryCost":0.0,
         "entryCostAsString":"0.00",
         "responseLabel":"user",
         "tournamentEnabled":false,
         "disabled":false,
         "resetEnabled":false,
         "timerOnModule":0,
         "runtimeDuration":0,
         "runtimeDurationOnInstance":0},
        {"singleton":true,
         "deployCode":0,
         "tag":"index/lobby",
         "type":"application",
         "typeId":"index",
         "subtypeId":"index-lobby",
         "category":"index",
         "capacity":0,
         "name":"Index",
         "description":"Index Service",
         "applicationId":"BDS01/764360f139244131a7e7db19dd02a10d",
         "accessMode":10,
         "entryCost":0.0,
         "entryCostAsString":"0.00",
         "responseLabel":"index",
         "tournamentEnabled":false,
         "disabled":false,
         "resetEnabled":false,
         "timerOnModule":0,
         "runtimeDuration":0
         ,"runtimeDurationOnInstance":0}]},
    {"descriptor":
     {"singleton":false,
      "deployCode":1,
      "tag":"admin/lobby",
      "type":"lobby",
      "typeId":"admin",
      "category":"service",
      "capacity":0,
      "name":"Admin",
      "description":"Tarantula Admin Service",
      "icon":"assets/gear.png",
      "viewId":"game.lobby",
      "applicationId":"BDS01/668ccbb1f7bf48b18f74fa8f2a3ef38d",
      "accessMode":12,
      "entryCost":0.0,
      "entryCostAsString":"0.00",
      "tournamentEnabled":false,
      "disabled":false,
      "resetEnabled":false,
      "timerOnModule":0,
      "runtimeDuration":0,
      "runtimeDurationOnInstance":0},"applications":[]},
    {"descriptor":
     {"singleton":false,
      "deployCode":1,
      "tag":"demo/lobby",
      "type":"lobby",
      "typeId":"demo",
      "category":"game",
      "capacity":0,
      "name":"DemoSync",
      "description":"Tarantula Demo Sync",
      "icon":"assets/duration.png",
      "viewId":"game.lobby",
      "applicationId":"BDS01/ce841fde95464434bec30aea6ce3cc8e",
      "accessMode":12,
      "entryCost":0.0,
      "entryCostAsString":"0.00",
      "responseLabel":"demo",
      "tournamentEnabled":false,
      "disabled":false,
      "resetEnabled":true,
      "timerOnModule":0,
      "runtimeDuration":0,
      "runtimeDurationOnInstance":0},"applications":[]}]}
```
### Reset AJAX POST /user/action
```
    -----REQUEST HTTP HEADERS----- 
    Accept : application/json
    Content-type :  application/x-www-form-urlencoded
    Tarantula-tag : index/user
    Tarantula-magic-key : deviceId
    Tarantula-action : onReset
    Tarantula-payload-size : 100
    
    ----RESPONSE HTTP HEADERS------
    Status Code: 200 OK
    Content-length: 2474
    Content-type: application/json
    
```
```JSON
    ------ SAMPLE RESPONSE CONTENT ------
{"command":"onLogin","code":200,"timestamp":0,"sequence":0,"successful":true,
 "presence":
 {"successful":true,
  "systemId":"BDS01/d9a4e069ab444428ba2c97c063a684dc",
  "stub":1,"token":"BDS01/d9a4e069ab444428ba2c97c063a684dc tarantula 1569410260021 9E90522AFD78F1B54D44C392A1F11B6C2E0C6B4E-1-1569404260020-38B5C99B0D32109672B606295B33EAE25C350689",
  "ticket":"tarantula 1569404270021 7209E063FD8BF497D33A1F1CF85552B56ABC5AAD",
  "balance":"0.00",
  "login":"abc123678"},
 "lobbyList":[
    {"descriptor":
     {"singleton":false,
      "deployCode":0,
      "type":"lobby",
      "typeId":"presence",
      "category":"service",
      "capacity":0,
      "name":"presence",
      "description":"Tarantula Presence Service",
      "applicationId":"BDS01/6a3cdf9110544c9d95900e7f3e508850",
      "accessMode":12,
      "entryCost":0.0,
      "entryCostAsString":"0.00",
      "tournamentEnabled":false,
      "disabled":false,
      "resetEnabled":false,
      "timerOnModule":0,
      "runtimeDuration":0,
      "runtimeDurationOnInstance":0},"applications":[]}]}
```
### Presence AJAX POST /service/action
```
    ------ REQUEST HTTP HEADERS
    Accept: application/json
    Content-Length: 54
    Content-type: application/x-www-form-urlencoded
    Tarantula-action: onPresence
    Tarantula-payload-size: 54
    Tarantula-tag: presence/lobby
    Tarantula-token: BDS01/60c160f040164a43a5eeb4c67b9151ff tarantula 1569410852731 5E1863493D9657916F3CDB085B06ED8E6E3BE49A-4-1569404852731-2359BE7B3033779E793C7EB765D90A366C9CF0AB
    
    ------- RESPONSE HTTP HEADERS
    Status Code: 200 OK
    Content-length: 437
    Content-type: application/json
```
```JSON
    ------ SAMPLE RESPONSE CONTENT ------
{"command":"onPresence","code":200,"timestamp":0,"sequence":0,"successful":true,
 "presence":
 {"successful":false,
  "systemId":"BDS01/60c160f040164a43a5eeb4c67b9151ff",
  "stub":0,
  "balance":"1007000.00"},
 "connection":
 {"command":"onConnect",
  "code":0,
  "timestamp":0,
  "sequence":0,
  "successful":false,
  "path":"tarantula",
  "protocol":"ws",
  "host":"10.0.0.29",
  "type":"websocket",
  "serverId":"da72b3f0-dfac-11e9-8148-af6b679bc459",
  "secured":false,
  "port":80}}
```
### Lobby AJAX
### PLAY AJAX
### LOGOUT AJAX
### SERVICE AJAX
### INSTANCE AJAX