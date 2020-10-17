using System;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using UnityEngine;

namespace GameClustering
{
    [CreateAssetMenu(fileName = "IntegrationManager", menuName = "GameClustering/IntegrationManager", order = 1)]
    public class IntegrationManager : ScriptableObject
    {
        private static readonly JsonSerializerSettings JsonSerializerSettings = new JsonSerializerSettings{NullValueHandling = NullValueHandling.Ignore};

        public string gecHost = "localhost:8090";
        private HttpCaller _httpCaller;
        public IMessenger Messenger { private set; get; }
        public Exception Exception { private set; get; }
        private bool _live;
        private string _deviceId;
        private static IntegrationManager _instance;

        public Presence Presence { get; private set; }
        public bool Authenticated => Presence == null;
        

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
                Debug.Log(response);
                return true;
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
                var pc = jo.SelectToken("connection");
                Presence = new Presence
                {
                    SystemId = (string)pt.SelectToken("systemId"),
                    Token = (string)pt.SelectToken("token"),
                    Ticket = (string)pt.SelectToken("ticket"),
                    Login =  (string)pt.SelectToken("login"),
                    Stub = (int)pt.SelectToken("stub")
                };
                if (pc == null)
                {
                    return true;
                }
                var connection = new Connection
                {
                    ConnectionId = (long)pc.SelectToken("connectionId"),
                    Type =  (string)pc.SelectToken("type"),
                    Host = (string)pc.SelectToken("host"),
                    Port = (int)pc.SelectToken("port"),
                    Secured = (bool)pc.SelectToken("secured")
                };
                Messenger = new UdpMessenger();
                Messenger.Connect(connection,Convert.FromBase64String((string)jo.SelectToken("serverKey")));
                _live = true;
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

        public async Task<bool> Ticket(MonoBehaviour caller)
        {
            try
            {
                var headers = new[]
                {
                    new Header {Name = Header.TarantulaTag, Value = "presence/lobby"},
                    new Header {Name = Header.TarantulaToken, Value = Presence.Token},
                    new Header {Name = Header.TarantulaAction, Value = "onTicket"}
                };
                var response = await _httpCaller.GetJson(caller, "/service/action", headers);
                Debug.Log(response);
                var jo = JObject.Parse(response);
                var suc = (bool)jo.SelectToken("successful");
                Presence.Ticket = (string)(jo.SelectToken("presence").SelectToken("ticket"));
                Debug.Log(Presence.Ticket);
                return suc;
            }
            catch (Exception ex)
            {
                Exception = ex;
                return false;
            }
        }
        
        public async Task OnMessage(){
            try
            {
                while(_live)
                {
                    await Messenger.ListenAsync();
                }
            }
            catch(Exception ex)
            {
                Exception = ex;
            }   
        }
        
    }
}