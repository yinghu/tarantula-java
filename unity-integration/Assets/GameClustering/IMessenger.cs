using System;
using System.Threading.Tasks;

namespace GameClustering
{
    public interface IMessenger
    {
        void Connect(Connection connection,byte[] key);
        void Disconnect();
        
        void Listen();
        Task<int> SendAsync(int type,int sequence,bool ack,DataBuffer payload);
        Task<int> SendAsync(int type,int sequence,bool ack);
        Task<int> RetryAsync();
        void RegisterMessageHandler(int type,int sequence,Action<DataBuffer> messageHandler);
        void UnregisterMessageHandler(int type,int sequence);

        int PendingMessages();
        int TotalOutbound();
        int TotalInbound();
        int TotalRetries();
        int TotalBytes();
    }
}