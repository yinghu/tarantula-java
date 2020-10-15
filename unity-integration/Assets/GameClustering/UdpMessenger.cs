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
        private readonly Dictionary<CallbackKey, Action<DataBuffer>> _handlers;
        private Connection _connection;
        private ICryptoTransform _encrypt;
        private ICryptoTransform _decrypt;
        private int _messageId;
        public UdpMessenger()
        {
            _handlers = new Dictionary<CallbackKey, Action<DataBuffer>>();
        }

        public void Connect(Connection connection,byte[] serverKey)
        {
            _connection = connection;
            if (_connection.Secured)
            {
                var rijAlg = new RijndaelManaged()
                {
                    Key = serverKey,
                    Padding = PaddingMode.PKCS7,
                    Mode = CipherMode.CBC,
                    IV = serverKey
                };
                _encrypt = rijAlg.CreateEncryptor();
                _decrypt = rijAlg.CreateDecryptor();
            }

            _udpClient = new UdpClient(_connection.Host,_connection.Port);
        }
        
        public async Task<bool> SendAsync(int type,int sequence,bool ack,DataBuffer payload)
        {
            using (var message = new OutboundMessage())
            {
                message.ConnectionId(_connection.ConnectionId);
                message.Ack(ack);
                message.Type(type);
                message.MessageId(_messageId++);
                message.Sequence(sequence);
                message.Timestamp(1000);
                message.Payload(payload.ToArray());
                var outMessage = _connection.Secured ? Encrypt(message.Message()) : message.Message();
                var bytes = await _udpClient.SendAsync(outMessage, outMessage.Length);
                return bytes > 0;
            }
        }

        public async Task ListenAsync(){
            var ret = await _udpClient.ReceiveAsync();
            if (ret.Buffer.Length > 0)
            {
                using (var inboundMessage = new InboundMessage(_connection.Secured ? Decrypt(ret.Buffer) : ret.Buffer))
                {
                    var callbackKey = new CallbackKey(inboundMessage.Type(),inboundMessage.Sequence());
                    if (_handlers.TryGetValue(callbackKey, out var handler))
                    {
                        using (var buffer = new DataBuffer(inboundMessage.Payload()))
                        {
                            handler.Invoke(buffer);    
                        }
                    }
                    else
                    {
                        Debug.Log("NO HANDLER REGISTERED->" + inboundMessage.Type()+"/"+inboundMessage.Sequence());
                    }
                }
            }    
            else
            {
                Debug.Log("NO INBOUND MESSAGE");
            }
        }

        public void RegisterMessageHandler(int type,int sequence,Action<DataBuffer> messageHandler)
        {
            _handlers[new CallbackKey(type,sequence)] = messageHandler;
        }
        
        public void UnregisterMessageHandler(int type,int sequence)
        {
            _handlers.Remove(new CallbackKey(type,sequence));
        }

        private byte[] Encrypt(byte[] data)
        {
            using (var stream = new MemoryStream())
            {
                var cryptStream = new CryptoStream(stream, _encrypt, CryptoStreamMode.Write);
                cryptStream.Write(data, 0, data.Length);
                cryptStream.FlushFinalBlock();
                return stream.ToArray();
            }
        }
        private byte[] Decrypt(byte[] data)
        {
            using (var stream = new MemoryStream())
            {
                var cryptStream = new CryptoStream(stream, _decrypt, CryptoStreamMode.Write);
                cryptStream.Write(data, 0, data.Length);
                cryptStream.FlushFinalBlock();
                return stream.ToArray();
            }
        }
    }
}