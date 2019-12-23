using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Board : MonoBehaviour{
    public GameObject redPlayer;
    public GameObject greenPlayer;
    public GameObject[] itemList;
    void Start(){
        
    }

    void Update(){
        
    }
    
    public void OnView(){
        Vector3 mp = new Vector3(400,720,0);
        Vector3 vc = Camera.main.ScreenToWorldPoint(mp);
        vc.z = 1;
        vc.y = -6f;
        GameObject r = Instantiate(redPlayer,vc,Quaternion.identity,transform);
        Setup r1 = r.GetComponent<Setup>();
        r1._Setup();
        mp = new Vector3(400,220,0);
        vc = Camera.main.ScreenToWorldPoint(mp);
        vc.z = 20;
        vc.y = -6f;
        GameObject g = Instantiate(greenPlayer,vc,Quaternion.identity,transform);
        Setup g1 = g.GetComponent<Setup>();
        g1._Setup();
        
    }
    public void OnViewB(){
        Vector3 mp = new Vector3(400,720,0);
        Vector3 vc = Camera.main.ScreenToWorldPoint(mp);
        //vc.z = vc.z*10;
        Debug.Log(vc);
        vc.z = 20;
        vc.y = -6f;
        GameObject r = Instantiate(itemList[0],vc,Quaternion.identity,transform);
        Setup r1 = r.GetComponent<Setup>();
        r1._Setup();    
    }
    public void OnViewC(){
        Vector3 mp = new Vector3(350,720,0);
        Vector3 vc = Camera.main.ScreenToWorldPoint(mp);
        Debug.Log(vc);
        vc.z = 10;
        vc.y = -6f;
        GameObject r = Instantiate(itemList[1],vc,Quaternion.identity,transform);
        Setup r1 = r.GetComponent<Setup>();
        r1._Setup(); 
    }
    public void OnViewD(){
        Vector3 mp = new Vector3(150,720,0);
        Vector3 vc = Camera.main.ScreenToWorldPoint(mp);
        Debug.Log(vc);
        vc.z = 15;
        vc.y = -6f;
        GameObject r = Instantiate(itemList[2],vc,Quaternion.identity,transform);
        Setup r1 = r.GetComponent<Setup>();
        r1._Setup();
    }
}
