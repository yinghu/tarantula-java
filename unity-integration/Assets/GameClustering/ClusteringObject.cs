using System;
using UnityEngine;

namespace GameClustering
{
    public class ClusteringObject : MonoBehaviour
    {
       
        public int sequence;
        private IntegrationManager _integrationManager;
        protected async void StartClusteringObject(Action<DataBuffer> onSync)
        {
            _integrationManager = IntegrationManager.Instance;
            Messenger.RegisterMessageHandler(MessageType.Sync,sequence,  (sessionId,data) =>
            {
                if (sessionId == _integrationManager.SessionId)
                {
                    return;
                }
                MainThread.Execute(async buffer =>
                {
                    buffer.PutVector3(transform.position);
                    await Messenger.SendAsync(MessageType.OnSync, sequence, true, buffer);
                });
            });
            Messenger.RegisterMessageHandler(MessageType.OnSync,sequence, (sessionId, data) =>
            {
                if (sessionId == _integrationManager.SessionId)
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
        
        protected IMessenger Messenger => _integrationManager.Messenger;
        protected static MessageContext MainThread => MessageContext.Instance;
    }
}