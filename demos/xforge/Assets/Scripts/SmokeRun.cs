using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class SmokeRun : SmokeBehavior
{
    // Start is called before the first frame update
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

    public void XA(){
        networkObject.SendRpc(RPC_MOVE_UP, Receivers.Owner);
    }
    public void XB(){
        networkObject.SendRpc(RPC_MOVE_DOWN, Receivers.Owner);
    }
	private void Update()
	{
		// If unity's Update() runs, before the object is
		// instantiated in the network, then simply don't
		// continue, otherwise a bug/error will happen.
		// 
		// Unity's Update() running, before this object is instantiated
		// on the network is **very** rare, but better be safe 100%
		if (networkObject == null){
			return;
        }
		if (Input.GetMouseButtonDown(0)) {
            RaycastHit hit;
            if (Physics.Raycast(Camera.main.ScreenPointToRay(Input.mousePosition), out hit)) {
                networkObject.SendRpc(RPC_MOVE, Receivers.Owner,hit.point);
            }   
        }
		// If we are not the owner of this network object then we should
		// move this cube to the position/rotation dictated by the owner
		if (!networkObject.IsOwner){
			transform.position = networkObject.position;
			transform.rotation = networkObject.rotation;
            return;
		}
		// Let the owner move the cube around with the arrow keys
		//transform.position += new Vector3(Input.GetAxis("Horizontal"), 0, Input.GetAxis("Vertical")).normalized * speed * Time.deltaTime;

		// If we are the owner of the object we should send the new position
		// and rotation across the network for receivers to move to in the above code
		networkObject.position = transform.position;
		networkObject.rotation = transform.rotation;

		// Note: Forge Networking takes care of only sending the delta, so there
		// is no need for you to do that manually
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
                rigidBody.MovePosition(rigidBody.position + movement);
            }
        }
    } 
    public override void Move(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            Debug.Log("move");
			target = args.GetNext<Vector3>();
            speed = 5.0f;
		});
    }
    public override void MoveUp(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            Debug.Log("move up");
            //index, position, rotation, repli
            NetworkManager.Instance.InstantiateSmoke(0,Vector3.zero,Quaternion.Euler(15,0,0),true);
			//transform.position += Vector3.forward;
		});
    }
    public override void MoveDown(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            Debug.Log("move down");
			//transform.position += Vector3.back;
            NetworkManager.Instance.InstantiateSmoke();
		});
    }
}
