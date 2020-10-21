using System;
using System.Collections.Concurrent;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Threading;
using System.Threading.Tasks;
using UnityEngine;

namespace GameClustering
{
    public class UdpMessenger : IMessenger
    {
        private UdpClient _udpClient;
        private readonly ConcurrentDictionary<CallbackKey, Action<DataBuffer>> _handlers;
        private readonly ConcurrentDictionary<int, PendingMessage> _pendingMessages;
        private Connection _connection;
        private ICryptoTransform _encrypt;
        private ICryptoTransform _decrypt;
        private int _messageId;
        private int _totalOutbound;
        private int _totalInbound;
        private int _totalBytes;
        private int _totalRetries;
        private bool _live;
        private IPEndPoint _remote;
        public UdpMessenger()
        {
            _handlers = new ConcurrentDictionary<CallbackKey, Action<DataBuffer>>();
            _pendingMessages = new ConcurrentDictionary<int, PendingMessage>();
            _live = false;
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
            _remote = new IPEndPoint(IPAddress.Parse(_connection.Host),_connection.Port);
            _udpClient = new UdpClient(_connection.Host,_connection.Port);
            _handlers[new CallbackKey(MessageType.Ack,0)] = buffer =>
            {
                var sz = buffer.GetInt();
                for (var i = 0; i < sz; i++)
                {
                    _pendingMessages.TryRemove(buffer.GetInt(),out var removed);
                }
            };
            _handlers[new CallbackKey(MessageType.Ping,0)] = async buffer =>
            {
                await SendAsync(MessageType.Pong, 0, false);
            };
            _live = true;
        }

        public void Disconnect()
        {
            _live = false;
            _totalOutbound = 0;
            _totalInbound = 0;
            _pendingMessages.Clear();
            _udpClient.Close();
        }

        public async Task<int> SendAsync(int type, int sequence, bool ack)
        {
            return await SendAsync(type, sequence, ack, null);
        }

        public async Task<int> SendAsync(int type,int sequence,bool ack,DataBuffer payload)
        {
            using (var message = new OutboundMessage())
            {
                message.ConnectionId(_connection.ConnectionId);
                message.Ack(ack);//cache if ack = true
                message.Type(type);
                var messageId = 0;
                if (ack)
                {
                    messageId = Interlocked.Increment(ref _messageId);
                    message.MessageId(messageId);
                }
                message.SessionId(_connection.SessionId);
                message.Sequence(sequence);
                var timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
                message.Timestamp(timestamp);
                if (payload != null)
                {
                    message.Payload(payload.ToArray());
                }
                var outMessage = _connection.Secured ? Encrypt(message.Message()) : message.Message();
                var bytes = await _udpClient.SendAsync(outMessage, outMessage.Length);
                if (ack && bytes > 0)
                {
                    _pendingMessages[messageId] = new PendingMessage {Data = outMessage, Timestamp = timestamp, Retries = 2};
                }
                _totalOutbound++;
                _totalBytes += bytes;
                return messageId;
            }
        }

        public async Task<bool> RetryAsync(int messageId)
        {
            if (!_pendingMessages.TryGetValue(messageId, out var outMessage))
            {
                return false;
            }
            await _udpClient.SendAsync(outMessage.Data, outMessage.Data.Length);
            if (--outMessage.Retries<=0)
            {
                _pendingMessages.TryRemove(messageId, out var removed);
            }
            _totalRetries++;
            return true;
        }

        public void Listen()
        {
            while (_live)
            {
                try
                {
                    var available = _udpClient.Available;
                    if (available > 0)
                    {
                        var bytes = _udpClient.Receive(ref _remote);
                        _totalInbound++;
                        _totalBytes += available;
                        ProcessMessage(bytes);
                    }
                    else
                    {
                        Thread.Sleep(50);
                    }
                }
                catch (Exception ex)
                {
                    Debug.Log(ex.Message); 
                }
            }    
        }

        public void RegisterMessageHandler(int type,int sequence,Action<DataBuffer> messageHandler)
        {
            _handlers[new CallbackKey(type,sequence)] = messageHandler;
        }
        
        public void UnregisterMessageHandler(int type,int sequence)
        {
            _handlers.TryRemove(new CallbackKey(type,sequence),out var removed);
        }

        public int PendingMessages()
        {
            return _pendingMessages.Count;
        }

        public int TotalOutbound()
        {
            return _totalOutbound;
        }

        public int TotalInbound()
        {
            return _totalInbound;
        }

        public int TotalBytes()
        {
            return _totalBytes;
        }

        public int TotalRetries()
        {
            return _totalRetries;
        }

        private void ProcessMessage(byte[] data)
        {
            using (var inboundMessage = new InboundMessage(_connection.Secured ? Decrypt(data) : data))
            {
                var callbackKey = new CallbackKey(inboundMessage.Type(),inboundMessage.Sequence());
                if (_handlers.TryGetValue(callbackKey, out var handler))
                {
                    if (inboundMessage.Type() == MessageType.Join)
                    {
                        _connection.SessionId = inboundMessage.SessionId();
                    }
                    else if (inboundMessage.Type() == MessageType.Leave)
                    {
                        _connection.SessionId = 0;
                    }
                    //Debug.Log("timestamp->"+(DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()-inboundMessage.Timestamp()));
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