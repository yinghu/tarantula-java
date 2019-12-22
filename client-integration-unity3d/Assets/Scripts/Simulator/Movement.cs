using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;
public class Movement : MonoBehaviour
{
    public float speed;
    private CharacterController _controller;
    private Vector3 move;
    private Vector3 target;
    private bool xMoving = false;
    private bool zMoving = false;
    const double DIFF = 0.05;
    const float STEP = 0.5f;
    void Start(){
        _controller = GetComponent<CharacterController>();
        move = Vector3.zero;
    }
    void Update(){
        if (Input.GetMouseButtonDown(0)) {
            RaycastHit hit;
            if (Physics.Raycast(Camera.main.ScreenPointToRay(Input.mousePosition), out hit)) {
                Debug.Log(transform.position+"<><><>"+hit.point+"<><><>"+Input.mousePosition);
                Debug.DrawRay(transform.position, transform.TransformDirection(Vector3.forward) * hit.distance, Color.white);
                target = hit.point;
                float dx = (hit.point.x - transform.position.x);
                float dz = (hit.point.z - transform.position.z);
                float ab = Math.Abs(dx)-Math.Abs(dz);
                if(ab>=0){//x measure 
                    float _dz = STEP*Math.Abs(dz/dx);
                    dx = dx>0?(STEP):(-1*(STEP));
                    dz = dz>0?(_dz):(_dz*(-1f)); 
                }
                else{//z mesure
                    float _dx = 0.5f*Math.Abs(dx/dz);
                    dz = dz>0?(STEP):(-1*(STEP));
                    dx = dx>0?(_dx):(_dx*(-1f));
                }
                move = new Vector3(dx*speed,0,dz*speed);
                move = Vector3.ClampMagnitude(move,speed);
                move.y = -9.8f;
                move *= Time.deltaTime;
                move = transform.TransformDirection(move);
                xMoving = true;
                zMoving = true;
            }
        }
    }
    void FixedUpdate(){   
        if(xMoving||zMoving){
            _controller.Move(move);
            if(xMoving&&(transform.position.x*target.x>0)&&(Math.Abs(transform.position.x - target.x)<DIFF)){
                Debug.Log(transform.position+"<x><x><x>"+target);
                xMoving = false;
            }
            if(zMoving&&(transform.position.z*target.z>0)&&(Math.Abs(transform.position.z - target.z)<DIFF)){
                Debug.Log(transform.position+"<z><z><z>"+target);
                zMoving = false;
            } 
            if(!xMoving&&!zMoving){
                Debug.Log(transform.position+"<x><y><z>"+target);
            }
        } 
    }
    

}
