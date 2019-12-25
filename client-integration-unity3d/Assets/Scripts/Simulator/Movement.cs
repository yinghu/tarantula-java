using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;
public class Movement : MonoBehaviour
{
    private float speed = 5.0f;
    private Vector3 target;
    private float targetBuffer = 1.5f;
    private float rotationSpeed = 15.0f;
    private float deceleration = 10.0f;
    private CharacterController _controller;
    
   
    void Start(){
        _controller = GetComponent<CharacterController>();
        target = Vector3.one;
    }
    public void OnMove(Vector3 mousePosition,float _speed){
        RaycastHit hit;
        if (Physics.Raycast(Camera.main.ScreenPointToRay(mousePosition), out hit)) {
          //Debug.Log(transform.position+"<1><1><1>"+hit.point+"<><><>"+mousePosition+"<><><>"+_speed);
          target = hit.point;
          speed = _speed;
          deceleration = 2*_speed;
          targetBuffer = 1.5f*(_speed/5.0f);
        }
    }
    void FixedUpdate(){   
        Vector3 movement = Vector3.zero;
        if(target != Vector3.one){
            Vector3 tpos = new Vector3(target.x,transform.position.y,target.z);
            Quaternion trot = Quaternion.LookRotation(tpos-transform.position);
            transform.rotation = Quaternion.Slerp(transform.rotation,trot,rotationSpeed*Time.deltaTime);
            movement = speed*Vector3.forward;
            movement = Vector3.ClampMagnitude(movement,speed);
            movement.y = -9.8f;
            movement = transform.TransformDirection(movement);
            movement *=Time.deltaTime;
            if(Vector3.Distance(target,transform.position)< targetBuffer){
                speed -= deceleration* Time.deltaTime;
                if(speed<=0){
                    //Debug.Log(transform.position+"<2><2><2>"+target+"<><><>"+speed);
                    target = Vector3.one;
                }
            }
            _controller.Move(movement);
        }
    }
    

}
