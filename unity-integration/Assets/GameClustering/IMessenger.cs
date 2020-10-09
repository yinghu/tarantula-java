using System;
using System.Threading.Tasks;

namespace GameClustering
{
    public interface IMessenger
    {
        void Connect(Connection connection,byte[] key);
        Task ListenAsync();
        Task<bool> SendAsync(int type,int sequence,bool ack,DataBuffer payload);
        void RegisterMessageHandler(int type,Action<DataBuffer> messageHandler);
        void UnregisterMessageHandler(int type);
    }
}