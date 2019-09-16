using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Sync : MonoBehaviour {

	public Demo demo;
    public string command;
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}
    public void OnMouseDown(){
        demo.Sync(command);
    }
}
