using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Tarantula.Networking;
public class View : MonoBehaviour{
    
    public GameObject blueSpin;
    public GameObject redSpin;
    public GameObject[] shots;
    
    void Start()
    {
        
    }

    void Update()
    {
        
    }
    public void OnBoard(JArray robotList){
        Vector3  v = new Vector3();
        v.x = Screen.width/2;
        v.y = 100;
        _OnView((string)robotList[0].SelectToken("questId"),v,blueSpin);
        Vector3  v1 = new Vector3();
        v1.x = Screen.width/2;
        v1.y = Screen.height-100;
        _OnView((string)robotList[1].SelectToken("questId"),v1,redSpin);
    }
    public void OnMove(string questId,Vector3 dest,float speed){
        _OnMove(questId,dest,speed);
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
        if(mg!=null){
            Spin spin = mg.GetComponent<Spin>();    
            spin.OnMove(v,speed);
        }
        else{
            Debug.Log("no move->"+questId);
        }
    }
    private void _OnView(string name,Vector3 v,GameObject src){
        Vector3 vc = Camera.main.ScreenToWorldPoint(v);
        vc.z = 0;
        GameObject clone = Instantiate(src,vc, Quaternion.identity,transform);
        clone.name = name;
    }
}
