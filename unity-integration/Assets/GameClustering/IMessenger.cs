using System.Threading.Tasks;

namespace GameClustering
{
    public interface IMessenger
    {
        void Connect(Connection connection);
        Task ReceiveAsync();
        Task<bool> SendAsync(OutboundMessage message);
        void RegisterMessageHandler(int type,IMessageHandler messageHandler);
    }
}