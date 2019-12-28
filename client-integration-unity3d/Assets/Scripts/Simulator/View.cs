using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Tarantula.Networking;
public class View : MonoBehaviour{
    
    public GameObject[] players;
    public GameObject[] shots;
    
    void Start(){
        
    }

    void Update(){
        
    }
    public void OnBoard(JArray robotList){
        Vector3  v = new Vector3();
        v.x = Screen.width/2;
        v.y = Screen.width/2;
        _OnView((string)robotList[0].SelectToken("questId"),v,players[0]);
        Vector3  v1 = new Vector3();
        v1.x = Screen.width/2;
        v1.y = Screen.height/2;
        _OnView((string)robotList[1].SelectToken("questId"),v1,players[1]);
    }
    public void OnMove(string questId,Vector3 dest,float speed){
        _OnMove(questId,dest,speed);
    }
    public void OnRemove(string questId){
       GameObject mg = GameObject.Find("/View/"+questId);
       Destroy(mg);    
    }
    public void OnMove(Payload pv){
        Vector3 mp = new Vector3();
        mp.x = float.Parse(pv.headers[0].value)*Screen.width;
        mp.y = float.Parse(pv.headers[1].value)*Screen.height;
        mp.z = float.Parse(pv.headers[2].value);
        float speed = float.Parse(pv.headers[3].value);
        string questId = pv.headers[4].value;
        _OnMove(questId,mp,speed);
    }
    public void OnView(string name,Vector3 v,int ix){
        _OnView(name,v,shots[ix]);
    }
    private void _OnMove(string questId,Vector3 v,float speed){
        GameObject mg = GameObject.Find("/View/"+questId);
        if(mg!=null&&mg.tag=="robot"){
            Movement mov = mg.GetComponent<Movement>();    
            mov.OnMove(v,speed);
        }
        else{
            //Debug.Log("missed game object->"+questId);
        }
    }
    private void _OnView(string name,Vector3 v,GameObject src){
        RaycastHit hit;
        if (Physics.Raycast(Camera.main.ScreenPointToRay(v),out hit)) {
            Vector3 vc = hit.point;
            vc.y = 9.8f;
            GameObject clone = Instantiate(src,vc, Quaternion.identity,transform);
            clone.name = name;
            Setup _setup = clone.GetComponent<Setup>();
            _setup._Setup();
        }
    }
}
