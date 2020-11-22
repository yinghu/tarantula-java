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
        private readonly ConcurrentDictionary<CallbackKey, Action<int,byte[]>> _handlers;
        private readonly ConcurrentDictionary<int, PendingMessage> _pendingMessages;
        private readonly ConcurrentDictionary<int, int> _pendingGateways;
        private readonly PendingAck _pendingAck;
        private Connection _connection;
        private Rijndael _cipher;
        private int _messageId;
        private int _totalOutbound;
        private int _totalInbound;
        private int _totalBytes;
        private int _totalRetries;
        private bool _live;
        private readonly long _timeout;
        private readonly int _waitingTimeout;
        private readonly int _pendingCount;
        private IPEndPoint _remote;
        public UdpMessenger()
        {
            _handlers = new ConcurrentDictionary<CallbackKey, Action<int,byte[]>>();
            _pendingMessages = new ConcurrentDictionary<int, PendingMessage>();
            _pendingGateways = new ConcurrentDictionary<int, int>();
            _pendingAck = new PendingAck(20);
            _live = false;
            _timeout = 200;
            _waitingTimeout = 1;
            _pendingCount = 10;
        }

        public void Connect(Connection connection,byte[] serverKey)
        {
            _connection = connection;
            if (_connection.Secured)
            {
                _cipher = new RijndaelManaged()
                {
                    Key = serverKey,
                    Padding = PaddingMode.PKCS7,
                    Mode = CipherMode.CBC,
                    IV = serverKey
                };
            }
            _remote = new IPEndPoint(IPAddress.Parse(_connection.Host),_connection.Port);
            _udpClient = new UdpClient(_connection.Host,_connection.Port);
            _handlers[new CallbackKey(MessageType.Ack,0)] = (sessionId,data) =>
            {
                using (var buffer = new DataBuffer(data))
                {
                    var sz = buffer.GetInt();
                    for (var i = 0; i < sz; i++)
                    {
                        _pendingMessages.TryRemove(buffer.GetInt(), out var removed);
                    }
                }
            };
            _handlers[new CallbackKey(MessageType.Ping,0)] = async (sessionId,buffer) =>
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
            _totalBytes = 0;
            _totalRetries = 0;
            _pendingMessages.Clear();
            _udpClient.Close();
        }

        public async Task<int> SendAsync(int type, int sequence, bool ack)
        {
            return await SendAsync(type, sequence, ack, new byte[0]);
        }

        public async Task<int> SendAsync(int type,int sequence,bool ack,DataBuffer payload)
        {
            return await SendAsync(type, sequence, ack, payload.ToArray());
        }

        public async Task<int> SendAsync(int type, int sequence, bool ack, byte[] payload)
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
                if (payload == null || payload.Length > 0)
                {
                    message.Payload(payload);
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

        public async Task<int> RetryAsync()
        {
            var retries = 0;
            var timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            foreach (var kv in _pendingMessages)
            {
                var retry = kv.Value;
                if (timestamp - retry.Timestamp < _timeout || retry.Retries < 0)
                {
                    continue;
                }
                await _udpClient.SendAsync(retry.Data, retry.Data.Length);
                retries++;
                retry.Timestamp = timestamp;
                retry.Retries--;
            }
            _totalRetries += retries;
            ClearPendingGateways();
            return retries;
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
                        Thread.Sleep(_waitingTimeout);
                    }
                }
                catch (Exception ex)
                {
                    Debug.Log(ex.StackTrace); 
                }
            }    
        }

        public void RegisterMessageHandler(int type,int sequence,Action<int,byte[]> messageHandler)
        {
            _handlers[new CallbackKey(type,sequence)] = messageHandler;
        }
        
        public void UnregisterMessageHandler(int type,int sequence)
        {
            _handlers.TryRemove(new CallbackKey(type,sequence),out var removed);
        }

        public void Join(int sessionId,int[] messageIdRange)
        {
            _connection.SessionId = sessionId;
            Interlocked.Add(ref _messageId,messageIdRange[0]);
        }

        public void Leave()
        {
            _connection.SessionId = 0;
        }

        public int Sequence()
        {
            return Interlocked.Increment(ref _messageId);
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
                    if (inboundMessage.Ack())
                    {
                        Ack(inboundMessage.MessageId());
                        var ret = _pendingGateways.AddOrUpdate(inboundMessage.MessageId(), 0, (k, v) => 1);
                        if (ret > 0)
                        {
                            return;
                        }
                        _pendingGateways.TryUpdate(inboundMessage.MessageId(), 1, 0);
                    }
                    handler.Invoke(inboundMessage.SessionId(),inboundMessage.Payload());
                }
                else
                {
                    Debug.Log("NO HANDLER REGISTERED->" + inboundMessage.Type()+"/"+inboundMessage.Sequence()+"/"+inboundMessage.MessageId());
                }
            }        
        }

        private void ClearPendingGateways()
        {
            foreach (var messageId in _pendingGateways.Keys)
            {
                if (_pendingGateways.AddOrUpdate(messageId, 0, (k, v) => v + 1) > _pendingCount)
                {
                    _pendingGateways.TryRemove(messageId, out var ignore);
                }
            }    
        }

        private byte[] Encrypt(byte[] data)
        {
            using (var stream = new MemoryStream())
            {
                var cryptStream = new CryptoStream(stream, _cipher.CreateEncryptor(), CryptoStreamMode.Write);
                cryptStream.Write(data, 0, data.Length);
                cryptStream.FlushFinalBlock();
                return stream.ToArray();
            }
        }
        
        private byte[] Decrypt(byte[] data)
        {
            using (var stream = new MemoryStream())
            {
                var cryptStream = new CryptoStream(stream, _cipher.CreateDecryptor(), CryptoStreamMode.Write);
                cryptStream.Write(data, 0, data.Length);
                cryptStream.FlushFinalBlock();
                return stream.ToArray();
            }
        }

        public void Ack()
        {
            using (var buffer = new DataBuffer())
            {
                var list = _pendingAck.List();
                buffer.PutInt(list.Count);
                foreach (var mid in list)
                {
                    buffer.PutInt(mid);
                }
                Task.FromResult(SendAsync(MessageType.Ack, 0, false, buffer));
            }    
        }
        private  void Ack(int messageId)
        {
            _pendingAck.Push(messageId);
            Ack();    
        }
    }
}