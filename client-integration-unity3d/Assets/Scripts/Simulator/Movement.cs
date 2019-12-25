using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;
public class Movement : MonoBehaviour
{
    private float speed = 10.0f;
    private Vector3 target;
    private float targetBuffer = 1.5f;
    private float rotationSpeed = 20.0f;
    private float deceleration = 20.0f;
    
    private CharacterController _controller;
    
   
    void Start(){
        _controller = GetComponent<CharacterController>();
        target = Vector3.one;
    }
    public void OnMove(Vector3 mousePosition,float _speed){
        RaycastHit hit;
        if (Physics.Raycast(Camera.main.ScreenPointToRay(mousePosition), out hit)) {
          Debug.Log(transform.position+"<><><>"+hit.point+"<><><>"+mousePosition+"<><><>"+_speed);
          target = hit.point;
          speed = _speed;
        }
    }
    void FixedUpdate(){   
        Vector3 movement = Vector3.zero;
        if(target != Vector3.one){
            Vector3 tpos = new Vector3(target.x,transform.position.y,target.z);
            Quaternion trot = Quaternion.LookRotation(tpos-transform.position);
            transform.rotation = Quaternion.Slerp(transform.rotation,trot,rotationSpeed*Time.deltaTime);
            movement = speed*Vector3.forward;
            movement = transform.TransformDirection(movement);
            if(Vector3.Distance(target,transform.position)< targetBuffer){
                speed -= deceleration* Time.deltaTime;
                if(speed<=0){
                    Debug.Log("coming close");
                    target = Vector3.one;
                }
            }
            movement *=Time.deltaTime;
            _controller.Move(movement);
        }
    }
    

}
