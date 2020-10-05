using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using UnityEngine;

namespace GameClustering
{
    public class UdpCaller
    {
        private UdpClient _udpClient;

        public void Connect(string host, int port)
        {
            _udpClient = new UdpClient(host,port);
        }
        public async Task<bool> Send(OutboundMessage outboundMessage)
        {
            var payload = outboundMessage.Message();
            var bytes = await _udpClient.SendAsync(payload,payload.Length); 
            return bytes>0;    
        }
        public async Task Receive(){
            var ret = await _udpClient.ReceiveAsync();
            if (ret.Buffer.Length > 0)
            {
               var inboundMessage = new InboundMessage(ret.Buffer);
               Debug.Log("ack->"+inboundMessage.Ack());
               Debug.Log("tid->"+inboundMessage.Type());
               Debug.Log("mid->"+inboundMessage.MessageId());
               Debug.Log("cid->"+inboundMessage.ConnectionId());
               Debug.Log("Payload->" + Encoding.UTF8.GetString(inboundMessage.Payload()));
            }
            else
            {
                Debug.Log("NO MESSAGE");
            }
        }
    }
}