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
        
        private TextMeshProUGUI timer;
       
        private int seatIndex;
        public BumpRun[] bumpRuns;
        public Movement[] movements;
        
        void Start(){
            integration = GameEngineCluster.Instance;
            GameObject tm = GameObject.Find("/UI/Timer");
            timer = tm.GetComponent<TextMeshProUGUI>();
            timer.SetText("00:00");
            bumpRuns[0].RegisterRpcCallback(BumpRun.RPC_ON_MOVE,(args)=>{
                OnMove(args);    
            });
            bumpRuns[1].RegisterRpcCallback(BumpRun.RPC_ON_MOVE,(args)=>{
                OnMove(args);
            });
            bumpRuns[0].RegisterRpcCallback(BumpRun.RPC_ON_BOMB,(args)=>{
                OnLive(args);    
            });
            bumpRuns[1].RegisterRpcCallback(BumpRun.RPC_ON_BOMB,(args)=>{
                OnLive(args);
            });
            if(integration.stub!=null){
                seatIndex = integration.stub.seat;
            }
            NetworkManager.Instance.objectInitialized +=(INetworkBehavior unityGameObject, NetworkObject obj)=>{
                //GameObject go = ((MonoBehaviour)unityGameObject).gameObject;
                //Debug.Log(((MonoBehaviour)unityGameObject).gameObject.name+" Created");    
            };
        }
        
        void Update(){
            if (Input.GetMouseButtonDown(0)) {
                RaycastHit hit;
                if (Physics.Raycast(Camera.main.ScreenPointToRay(Input.mousePosition), out hit)) {
                    bumpRuns[seatIndex].networkObject.SendRpc(BumpRun.RPC_ON_MOVE,Receivers.Owner,hit.point,seatIndex);
                }
                OnLive();
            }
        }
        
        public void OnMove(int sx){
            float x = Random.Range(0,1080f);
            float y = Random.Range(0,1920f);
            Vector3 mp = new Vector3(x,y,0);
            RaycastHit hit;
            if (Physics.Raycast(Camera.main.ScreenPointToRay(mp), out hit)) {
                movements[sx].OnMove(hit.point); 
            }    
        }
        public void OnLive(){
            float x = Random.Range(0,1080f);
            float y = Random.Range(0,1920f);
            Vector3 mp = new Vector3(x,y,0);
            RaycastHit hit;
            if (Physics.Raycast(Camera.main.ScreenPointToRay(mp), out hit)) {
                bumpRuns[seatIndex].networkObject.SendRpc(BumpRun.RPC_ON_BOMB,Receivers.Owner,hit.point);
                /**
                BombRun bm = (BombRun)NetworkManager.Instance.InstantiateBomb(0,hit.point,Quaternion.identity,true);
                bm.networkStarted +=(noj)=>{
                    bm.Setup(100,"mmm");
                    bm.Explode();
                };**/
            }
        }
        public void OnTimer(int m,int s){
            timer.SetText(m+":"+s); 
        }
        public void OnTimer(int tx){
            int m = tx/60;
            int s = (tx%60);
            timer.SetText(m+":"+s); 
        }
        public void OnEnd(){ 
            timer.SetText("00:00");
        }
        
        private void OnLive(RpcArgs args){
            BombRun bm = (BombRun)NetworkManager.Instance.InstantiateBomb(0,args.GetNext<Vector3>(),Quaternion.identity,true);
            //string nm = args.GetNext<string>();
            bm.networkStarted +=(noj)=>{
                bm.Setup(1,"name");
            };
            //bm.gameObject.name = nm;
        }
        private void OnMove(RpcArgs args){
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
