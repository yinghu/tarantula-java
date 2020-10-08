using System;
using System.Threading.Tasks;

namespace GameClustering
{
    public interface IMessenger
    {
        void Connect(Connection connection,byte[] key);
        Task ListenAsync();
        Task<bool> SendAsync(int type,int sequence,bool ack,byte[] payload);
        void RegisterMessageHandler(int type,Action<InboundMessage> messageHandler);
        void UnregisterMessageHandler(int type);
    }
}