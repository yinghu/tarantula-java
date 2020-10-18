using System;
using System.Threading.Tasks;

namespace GameClustering
{
    public interface IMessenger
    {
        void Connect(Connection connection,byte[] key);
        void Disconnect();
        Task ListenAsync();
        Task<bool> SendAsync(int type,int sequence,bool ack,DataBuffer payload);
        Task<bool> SendAsync(int type,int sequence,bool ack);
        void RegisterMessageHandler(int type,int sequence,Action<DataBuffer> messageHandler);
        void UnregisterMessageHandler(int type,int sequence);
    }
}