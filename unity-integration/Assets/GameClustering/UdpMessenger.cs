using System;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Threading.Tasks;
using UnityEngine;

namespace GameClustering
{
    public class UdpMessenger : IMessenger
    {
        private UdpClient _udpClient;
        private readonly Dictionary<int, Action<InboundMessage>> _handlers;
        private Connection _connection;
        public UdpMessenger()
        {
            _handlers = new Dictionary<int, Action<InboundMessage>>();
        }

        public void Connect(Connection connection)
        {
            _connection = connection;
            _udpClient = new UdpClient(_connection.Host,_connection.Port);
        }
        
        public async Task<bool> SendAsync(OutboundMessage outboundMessage)
        {
            var payload = outboundMessage.Message();
            var bytes = await _udpClient.SendAsync(payload,payload.Length); 
            return bytes>0;
        }
        
        public async Task ListenAsync(){
            var ret = await _udpClient.ReceiveAsync();
            if (ret.Buffer.Length > 0)
            {
               var inboundMessage = new InboundMessage(ret.Buffer);
               if(_handlers.TryGetValue(inboundMessage.Type(),out var handler)){
                   handler.Invoke(inboundMessage);
               }
               else
               {
                   Debug.Log("NO HANDLER REGISTERED->"+inboundMessage.Type());
               }
            }
            else
            {
                Debug.Log("NO MESSAGE");
            }
        }

        public void RegisterMessageHandler(int type,Action<InboundMessage> messageHandler)
        {
            _handlers[type] = messageHandler;
        }
        
        public void UnregisterMessageHandler(int type)
        {
            _handlers.Remove(type);
        }
    }
}