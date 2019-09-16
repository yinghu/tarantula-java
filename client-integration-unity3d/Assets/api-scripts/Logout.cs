using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Logout : MonoBehaviour {

	public TARA_API api;
    public Balance balance;
	void Start () {
		
	}
	
	void Update () {
		
	}
    public void OnMouseDown(){
        api.Logout((a)=>{
            if(a){
                balance.ClearBalance();
            }
        });
    }
}
