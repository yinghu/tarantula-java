using System;
using System.Threading.Tasks;

namespace GameClustering
{
    public interface IMessenger
    {
        void Connect(Connection connection,byte[] key);
        void Disconnect();
        
        void Listen();
        void ListenAsync();
        Task<int> SendAsync(int type,int sequence,bool ack,DataBuffer payload);
        Task<int> SendAsync(int type,int sequence,bool ack,byte[] payload);
        Task<int> SendAsync(int type,int sequence,bool ack);
        
        int Send(int type,int sequence,bool ack,DataBuffer payload);
        int Send(int type,int sequence,bool ack,byte[] payload);
        int Send(int type,int sequence,bool ack);
        
        Task<int> RetryAsync();
        Task AckAsync();
        void Ack();
        void RegisterMessageHandler(int type,int sequence,Action<int,byte[]> messageHandler);
        void UnregisterMessageHandler(int type,int sequence);

        void Join(int sessionId,int[] messageIdRange);
        void Leave();
        int Sequence();

        int PendingMessages();
        int TotalOutbound();
        int TotalInbound();
        int TotalRetries();
        int TotalBytes();
    }
}