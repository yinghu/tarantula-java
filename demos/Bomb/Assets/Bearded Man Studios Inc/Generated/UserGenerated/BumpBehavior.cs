using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
using UnityEngine;

namespace BeardedManStudios.Forge.Networking.Generated
{
	[GeneratedRPC("{\"types\":[[\"Vector3\", \"int\"][\"int\"][\"float\"][\"Vector3\", \"string\"][\"string\"][]]")]
	[GeneratedRPCVariableNames("{\"types\":[[\"destination\", \"index\"][\"int\"][\"foo\"][\"position\", \"string\"][\"oid\"][]]")]
	public abstract partial class BumpBehavior : NetworkBehavior
	{
		public const byte RPC_ON_MOVE = 0 + 5;
		public const byte RPC_ON_LIVE = 1 + 5;
		public const byte RPC_ON_DAMAGE = 2 + 5;
		public const byte RPC_ON_QUEST = 3 + 5;
		public const byte RPC_ON_REMOVE = 4 + 5;
		public const byte RPC_ON_EXPLODE = 5 + 5;
		
		public BumpNetworkObject networkObject = null;

		public override void Initialize(NetworkObject obj)
		{
			// We have already initialized this object
			if (networkObject != null && networkObject.AttachedBehavior != null)
				return;
			
			networkObject = (BumpNetworkObject)obj;
			networkObject.AttachedBehavior = this;

			base.SetupHelperRpcs(networkObject);
			networkObject.RegisterRpc("OnMove", OnMove, typeof(Vector3), typeof(int));
			networkObject.RegisterRpc("OnLive", OnLive, typeof(int));
			networkObject.RegisterRpc("OnDamage", OnDamage, typeof(float));
			networkObject.RegisterRpc("OnQuest", OnQuest, typeof(Vector3), typeof(string));
			networkObject.RegisterRpc("OnRemove", OnRemove, typeof(string));
			networkObject.RegisterRpc("OnExplode", OnExplode);

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

		/// <summary>
		/// Arguments:
		/// Vector3 destination
		/// int index
		/// </summary>
		public abstract void OnMove(RpcArgs args);
		/// <summary>
		/// Arguments:
		/// int int
		/// </summary>
		public abstract void OnLive(RpcArgs args);
		/// <summary>
		/// Arguments:
		/// float foo
		/// </summary>
		public abstract void OnDamage(RpcArgs args);
		/// <summary>
		/// Arguments:
		/// Vector3 position
		/// string string
		/// </summary>
		public abstract void OnQuest(RpcArgs args);
		/// <summary>
		/// Arguments:
		/// string oid
		/// </summary>
		public abstract void OnRemove(RpcArgs args);
		/// <summary>
		/// Arguments:
		/// </summary>
		public abstract void OnExplode(RpcArgs args);

		// DO NOT TOUCH, THIS GETS GENERATED PLEASE EXTEND THIS CLASS IF YOU WISH TO HAVE CUSTOM CODE ADDITIONS
	}
}