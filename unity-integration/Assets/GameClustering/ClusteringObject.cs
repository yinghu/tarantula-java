using System;
using UnityEngine;

namespace GameClustering
{
    public class ClusteringObject : MonoBehaviour
    {
        public int typeId;
        public int sequence;
        public bool master;
        protected async void StartClusteringObject(Action<DataBuffer> outboundSync,Action<DataBuffer> onSync)
        {
            Messenger.RegisterMessageHandler(MessageType.Sync,sequence,  (sessionId,data) =>
            {
                if (sessionId == Manager.SessionId)
                {
                    return;
                }
                MainThread.Execute( buffer =>
                {
                    outboundSync?.Invoke(buffer);
                });
            });
            Messenger.RegisterMessageHandler(MessageType.OnSync,sequence, (sessionId, data) =>
            {
                if (sessionId == Manager.SessionId)
                {
                    return;
                }
                MainThread.Execute(data, buffer =>
                {
                    onSync?.Invoke(buffer);
                });    
            });
            await Messenger.SendAsync(MessageType.Sync, sequence, true);
        }
        
        protected static IMessenger Messenger => IntegrationManager.Instance.Messenger;
        protected static MessageContext MainThread => MessageContext.Instance;
        protected static IntegrationManager Manager => IntegrationManager.Instance;

        public virtual void Setup(int objId,bool owner)
        {
            sequence = objId;
            master = owner;
        }
    }
}