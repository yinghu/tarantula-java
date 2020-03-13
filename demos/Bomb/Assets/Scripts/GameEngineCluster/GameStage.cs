using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using TMPro;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
namespace Tarantula.Networking{
    public class GameStage : MonoBehaviour{

        private GameEngineCluster integration;
        public GameObject explosion;
        public GameObject[] itemList;
        private Descriptor game;
        
        private TextMeshProUGUI timer;
       
        private int seatIndex;
        public BumpRun[] bumpRuns;
        public Movement[] movements;
        
        void Start(){
            integration = GameEngineCluster.Instance;
            GameObject tm = GameObject.Find("/UI/Timer");
            timer = tm.GetComponent<TextMeshProUGUI>();
            timer.SetText("00:00");
            bumpRuns[0].OnQuestRPC += OnLive;
            bumpRuns[1].OnQuestRPC += OnLive;
            bumpRuns[0].OnMoveRPC += OnMove;
            bumpRuns[1].OnMoveRPC += OnMove;
            bumpRuns[0].OnRemoveRPC += OnDead;
            bumpRuns[1].OnRemoveRPC += OnDead;
            if(integration.room!=null){
                seatIndex = integration.room.seatIndex;
                game = integration.game;
            }
            NetworkManager.Instance.objectInitialized +=(INetworkBehavior unityGameObject, NetworkObject obj)=>{
                //GameObject go = ((MonoBehaviour)unityGameObject).gameObject;
                Debug.Log(((MonoBehaviour)unityGameObject).gameObject.name+" Created");    
            };
        }
        
        void Update(){
            if (Input.GetMouseButtonDown(0)) {
                RaycastHit hit;
                if (Physics.Raycast(Camera.main.ScreenPointToRay(Input.mousePosition), out hit)) {
                    bumpRuns[seatIndex].OnRun(hit.point,seatIndex);
                }   
            }
        }
        public async Task<bool> OnMove(Payload payload){
            OutboundMessage<Payload> om = new OutboundMessage<Payload>();
            om.label = "rq";
            om.query = payload.command;
            om.instanceId = game.gameId;
            om.payload = payload;
            bool suc = await integration.SendOnUDP(om);
            if(!suc){
                suc = await integration.SendOnInstance(game.applicationId,game.instanceId,payload,true);
            }
            return suc;
        }
        public void OnMove(string msg){
            Debug.Log(msg);
            JObject jo = JObject.Parse(msg);
            Vector3 mp = new Vector3();
            mp.x = ((float)jo.SelectToken("x"))*Screen.width;
            mp.y = ((float)jo.SelectToken("y"))*Screen.height;
            mp.z = 0;
            float speed = (float)jo.SelectToken("f");
            if(jo.ContainsKey("i")){
                int sx = (int)jo.SelectToken("i");
                if(sx!=seatIndex){
                    RaycastHit hit;
                    if (Physics.Raycast(Camera.main.ScreenPointToRay(mp), out hit)) {
                        bumpRuns[sx].OnRun(hit.point,sx);
                    }
                }
            }
        }
        public void OnTimer(string msg){
            JObject jo = JObject.Parse(msg);
            int m = (int)jo.SelectToken("m");
            int s = (int)jo.SelectToken("s");
            timer.SetText(m+":"+s); 
            
        }
        public void OnEnd(){ 
            timer.SetText("00:00");
        }
        public void OnQuest(string msg){
            JObject jo = JObject.Parse(msg);
            Vector3 mp = new Vector3();
            mp.x = ((float)jo.SelectToken("x"))*Screen.width;
            mp.y = ((float)jo.SelectToken("y"))*Screen.height;
            mp.z = 0;//float.Parse(pv.headers[2].value);
            string _name = (string)jo.SelectToken("n");
            int _ix = (int)jo.SelectToken("i");
            _OnView(_name,mp,itemList[_ix]);
        }
        public void OnRemove(string msg){
            JObject jo = JObject.Parse(msg);
            string questId = (string)jo.SelectToken("n");  
            bumpRuns[seatIndex].OnRemove(questId);
        }
        public void OnMessage(InboundMessage ibm){
            if(ibm.query!=null&&ibm.query.Equals("onMessage")){
                JObject jo = JObject.Parse(ibm.payload);
                Payload pv = jo.ToObject<Payload>();
                Vector3 mp = new Vector3();
                mp.x = float.Parse(pv.headers[0].value)*Screen.width;
                mp.y = float.Parse(pv.headers[1].value)*Screen.height;
                mp.z = 0;//float.Parse(pv.headers[2].value);
                float speed = float.Parse(pv.headers[3].value);
                int sx = int.Parse(pv.headers[4].value);
                //movements[sx].OnMove(mp,speed);   
            }
        }
        private void _OnView(string name,Vector3 v,GameObject src){
            RaycastHit hit;
            if (Physics.Raycast(Camera.main.ScreenPointToRay(v),out hit)) {
                Vector3 vc = hit.point;
                vc.y = 9.8f;
                bumpRuns[seatIndex].OnQuest(hit.point,name);
            }
            foreach(KeyValuePair<uint,NetworkObject> kv in NetworkManager.Instance.Networker.NetworkObjects){
                Debug.Log(">>>>>>>>>>KEY->"+kv.Key);
                Debug.Log(">>>>>>>>>>vALUE->"+((MonoBehaviour)kv.Value.AttachedBehavior).gameObject.name);
            }
        }
        public void OnLive(RpcArgs args){
            BombRun bm = (BombRun)NetworkManager.Instance.InstantiateBomb(0,args.GetNext<Vector3>(),Quaternion.identity,true);
            string nm = args.GetNext<string>();
            bm.networkStarted +=(noj)=>{
                bm.Setup(1,nm);
            };
            bm.gameObject.name = nm;
        }
        public void OnMove(RpcArgs args){
            Vector3 p = args.GetNext<Vector3>();
            int sx = args.GetNext<int>();
            movements[sx].OnMove(p); 
        }
        public void OnDead(RpcArgs args){
            string p = args.GetNext<string>();
            GameObject obj = GameObject.Find("/Stage/"+p);
            if(obj!=null){
                obj.GetComponent<BombRun>().Explode();
            } 
        }
    }
}
