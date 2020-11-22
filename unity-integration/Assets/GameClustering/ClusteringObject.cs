using UnityEngine;

namespace GameClustering
{
    public class ClusteringObject : MonoBehaviour
    {
       
        public int sequence;
        private IntegrationManager _integrationManager;
        protected async void StartClusteringObject()
        {
            _integrationManager = IntegrationManager.Instance;
            Messenger.RegisterMessageHandler(MessageType.Sync,sequence, async (sessionId,data) =>
            {
                if (sessionId == _integrationManager.SessionId)
                {
                    return;
                }
                using (var buffer = new DataBuffer())
                {
                    buffer.PutVector3(transform.position);
                    await Messenger.SendAsync(MessageType.Relay, sequence, true, buffer);
                }   
            });
            Messenger.RegisterMessageHandler(MessageType.Relay,sequence, (sessionId, data) =>
            {
                if (sessionId == _integrationManager.SessionId)
                {
                    return;
                }
                MainThread.Execute(data, buffer =>
                {
                    transform.position = buffer.GetVector3();
                });    
            });
            await Messenger.SendAsync(MessageType.Sync, sequence, true);
        }
        
        protected IMessenger Messenger => _integrationManager.Messenger;
        protected static MessageContext MainThread => MessageContext.Instance;
    }
}