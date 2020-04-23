using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
using UnityEngine;
using System;
using System.Collections;
using System.Collections.Generic;

namespace BeardedManStudios.Forge.Networking.Generated
{
    [GeneratedRPC("{\"types\":[[\"Vector3\", \"int\"][\"Vector3\", \"int\", \"string\"]]")]
	[GeneratedRPCVariableNames("{\"types\":[[\"destination\", \"index\"][\"position\", \"id\", \"key\"]]")]
	public abstract partial class BumpBehavior : NetworkBehavior
	{
		public const byte RPC_ON_MOVE = 0 + 5;
		public const byte RPC_ON_BOMB = 1 + 5;
		
		public BumpNetworkObject networkObject = null;
        public Dictionary<byte,Action<RpcArgs>> _rpc = new Dictionary<byte,Action<RpcArgs>>();
	
		public override void Initialize(NetworkObject obj)
		{
			// We have already initialized this object
			if (networkObject != null && networkObject.AttachedBehavior != null)
				return;
			
			networkObject = (BumpNetworkObject)obj;
			networkObject.AttachedBehavior = this;

			base.SetupHelperRpcs(networkObject);
			networkObject.RegisterRpc("OnMove", OnMove, typeof(Vector3), typeof(int));
			networkObject.RegisterRpc("OnBomb", OnBomb, typeof(Vector3), typeof(int), typeof(string));

			networkObject.onDestroy += DestroyGameObject;

			if (!obj.IsOwner)
			{
				if (!skipAttachIds.ContainsKey(obj.NetworkId)){
					uint newId = obj.NetworkId + 1;
					ProcessOthers(gameObject.transform, ref newId);
				}
				else
					skipAttachIds.Remove(obj.NetworkId);
			}

			if (obj.Metadata != null)
			{
				byte transformFlags = obj.Metadata[0];

				if (transformFlags != 0)
				{
					BMSByte metadataTransform = new BMSByte();
					metadataTransform.Clone(obj.Metadata);
					metadataTransform.MoveStartIndex(1);

					if ((transformFlags & 0x01) != 0 && (transformFlags & 0x02) != 0)
					{
						MainThreadManager.Run(() =>
						{
							transform.position = ObjectMapper.Instance.Map<Vector3>(metadataTransform);
							transform.rotation = ObjectMapper.Instance.Map<Quaternion>(metadataTransform);
						});
					}
					else if ((transformFlags & 0x01) != 0)
					{
						MainThreadManager.Run(() => { transform.position = ObjectMapper.Instance.Map<Vector3>(metadataTransform); });
					}
					else if ((transformFlags & 0x02) != 0)
					{
						MainThreadManager.Run(() => { transform.rotation = ObjectMapper.Instance.Map<Quaternion>(metadataTransform); });
					}
				}
			}

			MainThreadManager.Run(() =>
			{
				NetworkStart();
				networkObject.Networker.FlushCreateActions(networkObject);
			});
		}

		protected override void CompleteRegistration()
		{
			base.CompleteRegistration();
			networkObject.ReleaseCreateBuffer();
		}

		public override void Initialize(NetWorker networker, byte[] metadata = null)
		{
			Initialize(new BumpNetworkObject(networker, createCode: TempAttachCode, metadata: metadata));
		}

		private void DestroyGameObject(NetWorker sender)
		{
			MainThreadManager.Run(() => { try { Destroy(gameObject); } catch { } });
			networkObject.onDestroy -= DestroyGameObject;
		}

		public override NetworkObject CreateNetworkObject(NetWorker networker, int createCode, byte[] metadata = null)
		{
			return new BumpNetworkObject(networker, this, createCode, metadata);
		}

		protected override void InitializedTransform()
		{
			networkObject.SnapInterpolations();
		}
        public void RegisterRpcCallback(byte mid,Action<RpcArgs> callback){
            _rpc.Add(mid,callback);
        }
		/// <summary>
		/// Arguments:
		/// Vector3 destination
		/// int index
		/// </summary>
		public virtual void OnMove(RpcArgs args){
            MainThreadManager.Run(() =>{
                if(_rpc.ContainsKey(args.Id)){
                    _rpc[args.Id].Invoke(args);
                }
            });
        }
		/// <summary>
		/// Arguments:
		/// Vector3 position
		/// int id
		/// string key
		/// </summary>
		public virtual void OnBomb(RpcArgs args){
            MainThreadManager.Run(() =>{
                if(_rpc.ContainsKey(args.Id)){
                    _rpc[args.Id].Invoke(args);
                }
            });
        }

		// DO NOT TOUCH, THIS GETS GENERATED PLEASE EXTEND THIS CLASS IF YOU WISH TO HAVE CUSTOM CODE ADDITIONS
	}
}