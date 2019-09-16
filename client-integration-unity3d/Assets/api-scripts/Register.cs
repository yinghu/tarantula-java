using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Register : MonoBehaviour {

    public TARA_API api;
    
	void Start () {
		
	}
	
	void Update () {
		
	}
    public void OnMouseDown(){
        api.Register("aaa","aaa","AndyQ",(m)=>{
            Debug.Log(m);
        });
    }
}
