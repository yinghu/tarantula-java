using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
namespace Tarantula.Networking{
    public class Movement : MonoBehaviour
    {
        private float speed = 5.0f;
        private Vector3 target;
        private float targetBuffer = 1.5f;
        private float rotationSpeed = 15.0f;
        private float deceleration = 10.0f;
        private CharacterController _controller;
        private Rigidbody rigidBody;
        private bool useController;
        
        void Start(){
            _controller = GetComponent<CharacterController>();
            useController = _controller!=null;
            if(!useController){
                rigidBody = GetComponent<Rigidbody>();
            }
                target = Vector3.one;
            }
        public void OnMove(Vector3 destination){
            target = destination;
            speed = 5.0f;
        } 
        void FixedUpdate(){   
            Vector3 movement = Vector3.zero;
            if(target != Vector3.one){
                movement = speed*Vector3.forward;
                movement = Vector3.ClampMagnitude(movement,speed);
                movement.y = -9.8f;
                movement = transform.TransformDirection(movement);
                movement *=Time.fixedDeltaTime;
                if(Vector3.Distance(target,transform.position)< targetBuffer){
                    speed -= deceleration* Time.deltaTime;
                    if(speed<=0){
                        target = Vector3.one;
                        speed = 5.0f;
                    }
                }
                else{
                    Vector3 tpos = new Vector3(target.x,transform.position.y,target.z);
                    Quaternion trot = Quaternion.LookRotation(tpos-transform.position);
                    transform.rotation = Quaternion.Slerp(transform.rotation,trot,rotationSpeed*Time.fixedDeltaTime);
                }
                if(useController){
                    _controller.Move(movement);
                }else{
                    rigidBody.MovePosition(rigidBody.position+movement);
                }
            }
        } 
    }
}
