using System;
using UnityEngine;

namespace GameClustering
{
    public class ClusteringObject : MonoBehaviour
    {
       
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
                MainThread.Execute(async buffer =>
                {
                    outboundSync?.Invoke(buffer);
                    await Messenger.SendAsync(MessageType.OnSync, sequence, true, buffer);
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
    }
}