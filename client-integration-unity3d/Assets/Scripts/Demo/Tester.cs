using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Tarantula.Networking;
public class Tester : MonoBehaviour
{
    public float speed = 3.0f;
    bool _active;
    void Start()
    {
        _active = false;
    }

   
    void Update()
    {
        if(_active){
            transform.Rotate(10,speed,10);
            //transform.position += Vector3.up * 0.01f;
            //transform.position += Vector3.right * 0.01f;
            //float horizontalInput = Input.GetAxis("Horizontal");
            //get the Input from Vertical axis
            //float verticalInput = Input.GetAxis("Vertical");
            //update the position
            //transform.position = transform.position + new Vector3(horizontalInput * speed * Time.deltaTime, verticalInput * speed * Time.deltaTime, 0);
        }    
    }
    public async void OnMouseDown(){
        _active = true;
        GameEngineCluster gec = new GameEngineCluster("http://localhost:8090");
        gec.OnException += (ex)=>{Debug.Log(ex);};
        bool suc = await gec.Index(this);
        if(!suc){
            //to do success
            //_active = false;
        }
    }

}
