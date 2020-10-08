using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Threading.Tasks;
using UnityEngine;

namespace GameClustering
{
    public class UdpMessenger : IMessenger
    {
        private UdpClient _udpClient;
        private readonly Dictionary<int, Action<InboundMessage>> _handlers;
        private Connection _connection;
        private ICryptoTransform _encrypt;
        private ICryptoTransform _decrypt;
        private int _messageId;
        public UdpMessenger()
        {
            _handlers = new Dictionary<int, Action<InboundMessage>>();
        }

        public void Connect(Connection connection,byte[] serverKey)
        {
            _connection = connection;
            var rijAlg = new RijndaelManaged()
            {
                Key = serverKey,
                Padding = PaddingMode.PKCS7,
                Mode = CipherMode.CBC,
                IV = serverKey
            };
            _encrypt = rijAlg.CreateEncryptor();
            _decrypt = rijAlg.CreateDecryptor();
            _udpClient = new UdpClient(_connection.Host,_connection.Port);
        }
        
        public async Task<bool> SendAsync(int type,int sequence,bool ack,byte[] payload)
        {
            var message = new OutboundMessage();
            message.ConnectionId(_connection.ConnectionId);
            message.Ack(ack);
            message.Type(type);
            message.MessageId(_messageId++);
            message.Sequence(EncryptSequence(sequence));
            message.Payload(payload);
            var outMessage = message.Message();
            message.Close();
            var bytes = await _udpClient.SendAsync(outMessage,outMessage.Length); 
            return bytes>0;
        }

        public async Task ListenAsync(){
            var ret = await _udpClient.ReceiveAsync();
            if (ret.Buffer.Length > 0)
            {
               var inboundMessage = new InboundMessage(ret.Buffer);
               if(_handlers.TryGetValue(inboundMessage.Type(),out var handler)){
                   Debug.Log("sequence->"+DecryptSequence(inboundMessage.Sequence()));
                   handler.Invoke(inboundMessage);
                   inboundMessage.Close();
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

        private byte[] EncryptSequence(int sequence)
        {
            using (var stream = new MemoryStream())
            { 
                var seq = BitConverter.GetBytes(sequence);
                if (BitConverter.IsLittleEndian)
                {
                    Array.Reverse(seq);
                }
                var cryptStream = new CryptoStream(stream, _encrypt, CryptoStreamMode.Write);
                cryptStream.Write(seq, 0, seq.Length);
                cryptStream.FlushFinalBlock();
                return stream.ToArray();
            }
        }
        private int DecryptSequence(byte[] sequence)
        {
            using (var stream = new MemoryStream())
            {
                var cryptStream = new CryptoStream(stream, _decrypt, CryptoStreamMode.Write);
                cryptStream.Write(sequence, 0, sequence.Length);
                cryptStream.FlushFinalBlock();
                var seq = stream.ToArray();
                if (BitConverter.IsLittleEndian)
                {
                    Array.Reverse(seq);
                }
                return BitConverter.ToInt32(seq, 0);
            }
        }
    }
}