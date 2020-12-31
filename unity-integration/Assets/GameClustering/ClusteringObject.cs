using System;
using UnityEngine;

namespace GameClustering
{
    public class ClusteringObject : MonoBehaviour
    {
        //assigned game type id as index
        public int typeId;
        //runtime assigned game object id as the clustering id
        public int sequence;
        //runtime assigned flag as the owner of the game object
        public bool master;
        protected async void OnSync(Action<DataBuffer> outboundSync,Action<DataBuffer> inboundSync)
        {
            Messenger.RegisterMessageHandler(MessageType.Sync,sequence,  (sessionId,data) =>
            {
                if (sessionId == Manager.SessionId)
                {
                    return;
                }
                MainThread.Execute( async buffer =>
                {
                    outboundSync?.Invoke(buffer);//push game status to remote
                    await Messenger.SendAsync(MessageType.OnSync,sequence, true, buffer);
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
                    inboundSync?.Invoke(buffer);//update local game state
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