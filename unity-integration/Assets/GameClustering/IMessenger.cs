using System;
using System.Threading.Tasks;

namespace GameClustering
{
    public interface IMessenger
    {
        void Connect(Connection connection);
        Task ListenAsync();
        Task<bool> SendAsync(OutboundMessage message);
        void RegisterMessageHandler(int type,Action<InboundMessage> messageHandler);
        void UnregisterMessageHandler(int type);
    }
}