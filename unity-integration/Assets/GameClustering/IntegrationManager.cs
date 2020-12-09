using System;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using UnityEngine;

namespace GameClustering
{
    public delegate void OnSessionEvent(int sessionId);

    public delegate void OnGameEvent();
    
    [CreateAssetMenu(fileName = "IntegrationManager", menuName = "GameClustering/IntegrationManager", order = 1)]
    public class IntegrationManager : ScriptableObject
    {
        public event OnSessionEvent OnJoinedEvent;
        public event OnSessionEvent OnLeftEvent;
        
        public event OnGameEvent OnGameStartEvent;

        public event OnGameEvent OnGameClosingEvent;
        
        private static readonly JsonSerializerSettings JsonSerializerSettings = new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore};

        public string gecHost = "localhost:8090";
        public string typeId = "game";
        public bool blocking = false;
        private HttpCaller _httpCaller;
        public IMessenger Messenger { private set; get; }
        public Exception Exception { private set; get; }
       
        private string _deviceId;
        private static IntegrationManager _instance;

        public Presence Presence { get; private set; }
        public Room Room { get; private set; }
        public bool Authenticated => Presence != null;
        public int SessionId { get; private set; }

        private Thread _thread;
        private Thread _retryThread;
        [RuntimeInitializeOnLoadMethod]
        private static void _Init(){
            _instance = Resources.Load<IntegrationManager>("IntegrationManager");
            _instance.Bootstrap();    
        }

        public static IntegrationManager Instance => _instance;

        private void Bootstrap()
        {
            _httpCaller = new HttpCaller(gecHost);
            _deviceId = SystemInfo.deviceUniqueIdentifier;
            ThreadPool.SetMinThreads(1, 1);
            ThreadPool.SetMaxThreads(4, 4);
        }
        public async Task<bool> Index(MonoBehaviour caller)
        {
            try{
                var headers = new []
                {
                    new Header{ Name = Header.TarantulaTag , Value = "index/user"},
                    new Header{ Name = Header.TarantulaAction ,Value = "onIndex"}
                };
                var response = await _httpCaller.GetJson(caller,"/user/action",headers);
                var jo = JObject.Parse(response);
                return  (bool)jo.SelectToken("successful");
            }
            catch(Exception ex)
            { 
                Exception = ex;
                return false;
            }
        }
        
        public  async Task<bool> Device(MonoBehaviour caller){
            try{
                var device = new Device{ DeviceId = _deviceId};
                var headers = new []
                {
                    new Header{ Name = Header.TarantulaTag,Value = "index/user"},
                    new Header{ Name = Header.TarantulaMagicKey, Value = device.DeviceId},
                    new Header{ Name = Header.TarantulaAction, Value = "onDevice"}
                };
                var json = JsonConvert.SerializeObject(device,JsonSerializerSettings);
                var response = await _httpCaller.PostJson(caller,"/user/action",headers,json);
                var jo = JObject.Parse(response);
                var suc = (bool)jo.SelectToken("successful");
                if (!suc)
                {
                    return false;
                }
                var pt = jo.SelectToken("presence");
                Presence = new Presence
                {
                    SystemId = (string)pt.SelectToken("systemId"),
                    Token = (string)pt.SelectToken("token"),
                    Ticket = (string)pt.SelectToken("ticket"),
                    Login =  (string)pt.SelectToken("login"),
                    Stub = (int)pt.SelectToken("stub")
                };
                return true;
            }
            catch(Exception ex)
            {
                Exception = ex;
                return false;
            }
        }

        public async Task<bool> Logout(MonoBehaviour caller)
        {
            try
            {
                var headers = new[]
                {
                    new Header {Name = Header.TarantulaTag, Value = "presence/lobby"},
                    new Header {Name = Header.TarantulaToken, Value = Presence.Token},
                    new Header {Name = Header.TarantulaAction, Value = "onAbsence"}
                };
                var response = await _httpCaller.GetJson(caller, "/service/action", headers);
                var jo = JObject.Parse(response);
                var suc = (bool)jo.SelectToken("successful");
                Presence = null;
                return suc;
            }
            catch (Exception ex)
            {
                Exception = ex;
                return false;
            }
        }

        private async Task<bool> Connect(MonoBehaviour caller)
        {
            try
            {
                var headers = new[]
                {
                    new Header {Name = Header.TarantulaTag, Value = "presence/lobby"},
                    new Header {Name = Header.TarantulaToken, Value = Presence.Token},
                    new Header {Name = Header.TarantulaAction, Value = "onConnection"}
                };
                var response = await _httpCaller.GetJson(caller, "/service/action", headers);
                var jo = JObject.Parse(response);
                var suc = (bool)jo.SelectToken("successful");
                if (!suc)
                {
                    Exception = new Exception("No connection");
                    return false;
                }
                Presence.Ticket = (string)jo.SelectToken("presence").SelectToken("ticket");
                var pc = jo.SelectToken("connection");
                var connection = new Connection
                {
                    ConnectionId = (long)pc.SelectToken("connectionId"),
                    Type =  (string)pc.SelectToken("type"),
                    Host = (string)pc.SelectToken("host"),
                    Port = (int)pc.SelectToken("port"),
                    Secured = (bool)pc.SelectToken("secured")
                };
                Connect(connection, (string) jo.SelectToken("serverKey"));
                return true;
            }
            catch (Exception ex)
            {
                Exception = ex;
                return false;
            }
        }

        private void Connect(Connection connection,string serverKey)
        {
            Messenger = new UdpMessenger();
            Messenger.Connect(connection,Convert.FromBase64String(serverKey));
            _thread = blocking?new Thread(Messenger.Listen):new Thread(Messenger.ListenAsync);
            _thread.Start();
            _retryThread = new Thread(Messenger.RetryAsync);
            _retryThread.Start();
            Messenger.RegisterMessageHandler(MessageType.Join,0, (sessionId,data) =>
            {
                using (var buffer = new DataBuffer(data))
                {
                    var joined = buffer.GetUTF8String().Equals("accepted");
                    if (joined)
                    {
                        SessionId = sessionId;
                        Messenger.Join(sessionId, new[] {buffer.GetInt(), buffer.GetInt()});
                        Messenger.Ack();
                    }
                    else
                    {
                        Debug.Log("session rejected");
                    }
                }
            });
            Messenger.RegisterMessageHandler(MessageType.OnJoined,0, (sessionId,buffer) =>
            {
                OnJoinedEvent?.Invoke(sessionId);
            });
            Messenger.RegisterMessageHandler(MessageType.Leave,0, (sessionId,buffer) =>
            {
                Messenger.Leave();
                Messenger.Disconnect();
                OnLeftEvent?.Invoke(sessionId);
            });
            Messenger.RegisterMessageHandler(MessageType.OnLeft,0, (sessionId,buffer) =>
            {
                Messenger.Disconnect();
                OnLeftEvent?.Invoke(sessionId);
            });
            Messenger.RegisterMessageHandler(MessageType.OnKickedOff,0,  (sessionId, buffer) =>
            {
                //await Leave();
                Debug.Log("KICKED OFF->"+sessionId);
            });
            Messenger.RegisterMessageHandler(MessageType.GameJoinTimeout,0, (sessionId, buffer) =>
            {
                Debug.Log("JOIN TIMEOUT->"+sessionId);
            });
            Messenger.RegisterMessageHandler(MessageType.GameStart,0, (sessionId, buffer) =>
            {
                Debug.Log("GAME START->"+sessionId);
                OnGameStartEvent?.Invoke();
            });
            Messenger.RegisterMessageHandler(MessageType.GameClosing,0, (sessionId, buffer) =>
            {
                Debug.Log("GAME Closing->"+sessionId);
                OnGameClosingEvent?.Invoke();
            });
            Messenger.RegisterMessageHandler(MessageType.GameOvertime,0, (sessionId, buffer) =>
            {
                Debug.Log("GAME overtime->"+sessionId);
            });
            Messenger.RegisterMessageHandler(MessageType.GameClose,0, (sessionId, buffer) =>
            {
                Debug.Log("GAME close->"+sessionId);
            });
            Messenger.RegisterMessageHandler(MessageType.GameEnd,0, (sessionId, buffer) =>
            {
                Debug.Log("GAME end->"+sessionId);
            });
        }
        
        public async Task<bool> Leave(MonoBehaviour caller)
        {
            await Messenger.SendAsync(MessageType.Leave, 0, true);
            var headers = new[]
            {
                new Header {Name = Header.TarantulaTag, Value = Room.Tag},
                new Header {Name = Header.TarantulaToken, Value = Presence.Token},
                new Header {Name = Header.TarantulaAction, Value = "onLeave"}
            };
            var response = await _httpCaller.GetJson(caller, "/service/action", headers);
            var jo = JObject.Parse(response);
            var suc = (bool)jo.SelectToken("successful");
            if (suc)
            {
                return true;
            }
            Exception = new Exception((string)jo.SelectToken("message"));
            return false;
        }

        public async Task<bool> Join(MonoBehaviour caller)
        {
            try
            {
                var headers = new[]
                {
                    new Header {Name = Header.TarantulaTag, Value = typeId+"/mmk"},
                    new Header {Name = Header.TarantulaToken, Value = Presence.Token},
                    new Header {Name = Header.TarantulaAction, Value = "onPlay"}
                };
                //var page = new Payload{Headers = new []{new Header{ Name = "page",Value = "1"}}};
                //var json = JsonConvert.SerializeObject(page,JsonSerializerSettings);
                var response = await _httpCaller.GetJson(caller, "/service/action", headers);
                Debug.Log(response);
                var jo = JObject.Parse(response);
                var suc = (bool)jo.SelectToken("successful");
                if (!suc)
                {
                    Exception = new Exception((string)jo.SelectToken("message"));
                    return false;
                }

                Room = new Room
                {
                    Id = (string) jo.SelectToken("stub").SelectToken("roomId"),
                    Tag = (string) jo.SelectToken("stub").SelectToken("tag"),
                    Seat = (int)jo.SelectToken("stub").SelectToken("seat"),
                    Capacity = (int)jo.SelectToken("stub").SelectToken("capacity"),
                    Arena = (string) jo.SelectToken("stub").SelectToken("arena")
                };
                var pc = jo.SelectToken("connection");
                var connection = new Connection
                {
                    ConnectionId = (long)pc.SelectToken("connectionId"),
                    Type =  (string)pc.SelectToken("type"),
                    Host = (string)pc.SelectToken("host"),
                    Port = (int)pc.SelectToken("port"),
                    Secured = (bool)pc.SelectToken("secured")
                };
                Presence.Ticket = (string)jo.SelectToken("ticket");
                Connect(connection,(string) jo.SelectToken("serverKey"));
                using (var buffer = new DataBuffer())
                {
                    buffer.PutInt(Presence.Stub);
                    buffer.PutUTF8String(Presence.Login);
                    buffer.PutUTF8String(Presence.Ticket);
                    buffer.PutInt(Room.Seat);
                    await Messenger.SendAsync(MessageType.Join, 0, true, buffer);
                }
                return true;
            }
            catch (Exception ex)
            {
                Exception = ex;
                return false;
            }    
        }

    }
}