using System.Net.Sockets;
using System.Threading.Tasks;

namespace GameClustering
{
    public class UdpCaller
    {
        private UdpClient _udpClient;

        public void Connect(string host, int port)
        {
            _udpClient = new UdpClient(host,port);
        }
        public async Task Receive(){
            var ret = await _udpClient.ReceiveAsync();
            if (ret.Buffer.Length > 0)
            {
               var message = new InboundMessage(ret.Buffer);
               
            }
        }
    }
}