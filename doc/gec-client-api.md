### CLIENT RESTFUL AJAX/JSON API LIST 
### Index AJAX GET /user/index
Request URL: http://localhost:8090/user/index
Request Method: GET
Status Code: 200 OK
Remote Address: [::1]:8090
Referrer Policy: no-referrer-when-downgrade
Content-length: 2474
Content-type: application/json
Date: Wed, 25 Sep 2019 16:10:00 GMT
Accept: application/json
Accept-Encoding: gzip, deflate, br
Accept-Language: en-US,en;q=0.9
Connection: keep-alive
Host: localhost:8090
Referer: http://localhost:8090/
Sec-Fetch-Mode: cors
Sec-Fetch-Site: same-origin
Tarantula-tag: index/lobby
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36 OPR/63.0.3368.94
```JSON
{"command":"index","code":200,"timestamp":0,"sequence":0,"successful":true,"lobbyList":[{"descriptor":{"singleton":false,"deployCode":0,"type":"lobby","typeId":"index","category":"service","capacity":0,"name":"Index","description":"Tarantula Lobby Index","applicationId":"BDS01/5d844d31e1b44e939724f4c426c6280e","accessMode":10,"entryCost":0.0,"entryCostAsString":"0.00","tournamentEnabled":false,"disabled":false,"resetEnabled":false,"timerOnModule":0,"runtimeDuration":0,"runtimeDurationOnInstance":0},"applications":[{"singleton":true,"deployCode":0,"tag":"index/user","type":"application","typeId":"index","subtypeId":"index-user","capacity":0,"name":"User","description":"Tarantula User Management","applicationId":"BDS01/42f2a72599224b11aa727f8aae119db0","accessMode":10,"entryCost":0.0,"entryCostAsString":"0.00","responseLabel":"user","tournamentEnabled":false,"disabled":false,"resetEnabled":false,"timerOnModule":0,"runtimeDuration":0,"runtimeDurationOnInstance":0},{"singleton":true,"deployCode":0,"tag":"index/lobby","type":"application","typeId":"index","subtypeId":"index-lobby","category":"index","capacity":0,"name":"Index","description":"Index Service","applicationId":"BDS01/764360f139244131a7e7db19dd02a10d","accessMode":10,"entryCost":0.0,"entryCostAsString":"0.00","responseLabel":"index","tournamentEnabled":false,"disabled":false,"resetEnabled":false,"timerOnModule":0,"runtimeDuration":0,"runtimeDurationOnInstance":0}]},{"descriptor":{"singleton":false,"deployCode":1,"tag":"admin/lobby","type":"lobby","typeId":"admin","category":"service","capacity":0,"name":"Admin","description":"Tarantula Admin Service","icon":"assets/gear.png","viewId":"game.lobby","applicationId":"BDS01/668ccbb1f7bf48b18f74fa8f2a3ef38d","accessMode":12,"entryCost":0.0,"entryCostAsString":"0.00","tournamentEnabled":false,"disabled":false,"resetEnabled":false,"timerOnModule":0,"runtimeDuration":0,"runtimeDurationOnInstance":0},"applications":[]},{"descriptor":{"singleton":false,"deployCode":1,"tag":"demo/lobby","type":"lobby","typeId":"demo","category":"game","capacity":0,"name":"DemoSync","description":"Tarantula Demo Sync","icon":"assets/duration.png","viewId":"game.lobby","applicationId":"BDS01/ce841fde95464434bec30aea6ce3cc8e","accessMode":12,"entryCost":0.0,"entryCostAsString":"0.00","responseLabel":"demo","tournamentEnabled":false,"disabled":false,"resetEnabled":true,"timerOnModule":0,"runtimeDuration":0,"runtimeDurationOnInstance":0},"applications":[]}]}
```
### Reset AJAX 
### Presence AJAX
### Lobby AJAX
### PLAY AJAX
### LOGOUT AJAX
### SERVICE AJAX
### INSTANCE AJAX