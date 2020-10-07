using System.Collections.Generic;
using System.Net.Sockets;
using System.Threading.Tasks;
using UnityEngine;

namespace GameClustering
{
    public class UdpMessenger : IMessenger
    {
        private UdpClient _udpClient;
        private readonly Dictionary<int, IMessageHandler> _handlers;

        public UdpMessenger()
        {
            _handlers = new Dictionary<int, IMessageHandler>();
        }

        public void Connect(Connection connection)
        {
            _udpClient = new UdpClient(connection.Host,connection.Port);
        }
        
        public async Task<bool> SendAsync(OutboundMessage outboundMessage)
        {
            var payload = outboundMessage.Message();
            var bytes = await _udpClient.SendAsync(payload,payload.Length); 
            return bytes>0;
        }
        
        public async Task ReceiveAsync(){
            var ret = await _udpClient.ReceiveAsync();
            if (ret.Buffer.Length > 0)
            {
               var inboundMessage = new InboundMessage(ret.Buffer);
               if(_handlers.TryGetValue(inboundMessage.Type(),out var handler)){
                   handler.Handle(inboundMessage);
               }
            }
            else
            {
                Debug.Log("NO MESSAGE");
            }
        }

        public void RegisterMessageHandler(int type,IMessageHandler messageHandler)
        {
            _handlers[type] = messageHandler;
        }
    }
}