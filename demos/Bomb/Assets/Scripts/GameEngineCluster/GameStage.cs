using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using TMPro;
namespace Tarantula.Networking{
    public class GameStage : MonoBehaviour{

        public GameEngineCluster integration;
        public GameObject explosion;
        public GameObject[] itemList;
        private Descriptor game;
        private bool started;
        private bool joined;
        private bool waited;
        private GameObject tm;
        private TextMeshProUGUI timer;
       
        private GameObject a;
        private GameObject d;
        private GameObject x;
        
        private int seatIndex;
        public BumpRun[] movements;
        
        void Start(){
            tm = GameObject.Find("/UI/Timer");
            a = GameObject.Find("/UI/A"); 
            d = GameObject.Find("/UI/D"); 
            x = GameObject.Find("/UI/EXIT"); 
            timer = tm.GetComponent<TextMeshProUGUI>();
            timer.SetText("00:00");
            started = false;  
            joined = false;
            waited = false;
        }

        async void Update(){
            //tm.SetActive(joined);
            a.SetActive(started);
            d.SetActive(started);
            if (started&&Input.GetMouseButtonDown(0)) {
                RaycastHit hit;
                if (Physics.Raycast(Camera.main.ScreenPointToRay(Input.mousePosition), out hit)) {
                    movements[seatIndex].OnRun(hit.point);
                }   
            }
            /**
            if (started&&Input.GetMouseButtonDown(0)) {
                 Vector3 target = Input.mousePosition;
                 //movements[seatIndex].OnMove(target,2);
                 float x = (target.x/Screen.width);
                 float y = (target.y/Screen.height);
                 Payload payload = new Payload();
                 payload.command = "onMessage";
                 payload.headers = new Header[5];
                 payload.headers[0]=new Header("x",x.ToString());
                 payload.headers[1]=new Header("y",y.ToString());
                 payload.headers[2]=new Header("z",target.z.ToString());
                 payload.headers[3]=new Header("f","5");
                 payload.headers[4]=new Header("n",seatIndex+"");
                 //payload.headers[4]=new Header("n",(string)INS.robotList[INS.seatIndex].SelectToken("questId"));
                 await OnMove(payload);//publish move destination
                 //Debug.Log("SEND ["+suc+"]");
            }**/
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
        public void OnJoin(JObject jo,Descriptor desc){
            JToken occ = jo.SelectToken("gameObject.occupation");
            seatIndex = (int)occ.SelectToken("seatIndex");
            int state = (int)occ.SelectToken("state");
            string arenaZone = (string)jo.SelectToken("gameObject.arenaZone");
            this.game = desc;
            this.joined = true;
            this.waited = true;
        }
        
        public void OnStart(string msg){
            this.started = true;
            this.waited = false;
            Debug.Log(msg);           
        }
        public void OnMove(string msg){
            //Debug.Log(msg);
            JObject jo = JObject.Parse(msg);
            Vector3 mp = new Vector3();
            mp.x = ((float)jo.SelectToken("x"))*Screen.width;
            mp.y = ((float)jo.SelectToken("y"))*Screen.height;
            mp.z = 0;
            float speed = (float)jo.SelectToken("f");
            if(jo.ContainsKey("i")){
                int sx = (int)jo.SelectToken("i");
                if(sx!=seatIndex){
                    //movements[sx].OnMove(mp,speed);
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
            started = false;
            waited = false;
            joined = false;
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
            GameObject obj = GameObject.Find("/Stage/"+questId);
            if(obj!=null){
                obj.GetComponent<Bomb>().Explode();
            }
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
                GameObject clone = Instantiate(src,vc, Quaternion.identity,transform);
                clone.name = name;
                Bomb bm = clone.GetComponent<Bomb>();
                //bm.Explode();
            }
        }
    }
}
