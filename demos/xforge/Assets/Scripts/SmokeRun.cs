using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class SmokeRun : MonoSmoke
{
    
    private float speed = 2.0f;
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

    public void XA(){
        networkObject.SendRpc(RPC_MOVE_UP, Receivers.Owner);
    }
    public void XB(){
        networkObject.SendRpc(RPC_MOVE_DOWN, Receivers.Owner);
    }
    public void OnRun(Vector3 dest){
        networkObject.SendRpc(RPC_MOVE, Receivers.Owner,dest);
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
                    speed = 2.0f;
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
    //RPC Callbacks 
    public override void Move(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            target = args.GetNext<Vector3>();
            speed = 2.0f;
		    //rigidBody.constraints = RigidbodyConstraints.FreezeAll;
            //rigidBody.detectCollisions = false;
            BulletRun brun = (BulletRun)NetworkManager.Instance.InstantiateSmoke(1,transform.position,Quaternion.Euler(0,0,0),true);        
            brun.OnRun(target);
            //rigidBody.isKinematic = false;
            //rigidBody.enable = true;
        });
    }
    public override void MoveUp(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            NetworkManager.Instance.InstantiateSmoke(0,Vector3.zero,Quaternion.Euler(15,0,0),true);
		});
    }
    public override void MoveDown(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            BulletRun brun = (BulletRun)NetworkManager.Instance.InstantiateSmoke(1,transform.position,Quaternion.Euler(0,0,0),true);        
            brun.OnRun(target);
        });
    }
}
