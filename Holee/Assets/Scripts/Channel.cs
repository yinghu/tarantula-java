using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using UnityEngine;

namespace Holee
{
    public delegate void OnMessage(MessageHeader messageHeader,byte[] message);
    public delegate void OnRequest(MessageHeader messageHeader,byte[] message);
    
    public delegate void OnJoin(int sessionId);

    public class Channel
    {
        public event OnMessage OnMessage;
        public event OnRequest OnRequest;

        public event OnJoin OnJoin;

        private const int AckSize = 10;
        private const int Retries = 3;
        
        private readonly UdpClient _udpClient;
        private IPEndPoint _ipEndPoint;
        private Rijndael _cipher;
        private MessageBuffer _outboundBuffer;
        private MessageBuffer _inboundBuffer;
        private byte[] _ping;
        private readonly Dictionary<string,RetryData> _pendingAckMessage;
        private readonly MessageHeader[] _pendingAck;
        private readonly MessageBuffer _ackOutboundBuffer;
        public Presence Presence { set; get; }
        public int ChannelId { set; get; }
        public int SessionId { set; get; }

        public int Timeout { set; get; }

        public byte[] ServerKey { set; get; }
        public string Host { set; get; }
        public int Port { set; get; }

        public bool Joined { private set; get; }

        public Channel()
        {
            _udpClient = new UdpClient();
            _pendingAckMessage = new Dictionary<string,RetryData>();
            _pendingAck = new MessageHeader[AckSize];
            for (var i = 0; i < AckSize; i++)
            {
                _pendingAck[i] = new MessageHeader();
            }
            _ackOutboundBuffer = new MessageBuffer();
        }

        public void Init()
        {
            _cipher = new RijndaelManaged
            {
                Key = ServerKey,
                Padding = PaddingMode.PKCS7,
                Mode = CipherMode.CBC,
                IV = ServerKey
            };
            _outboundBuffer = new MessageBuffer(_cipher);
            _inboundBuffer = new MessageBuffer(_cipher);
            _ipEndPoint = new IPEndPoint(IPAddress.Parse(Host), Port);
            _udpClient.Connect(_ipEndPoint);
            _udpClient.BeginReceive(ReceiveCallback, null);
            Debug.Log("Starting udp client on ["+Host+":"+Port+"]");
        }

        public MessageBuffer CreateMessageBuffer()
        {
            return new MessageBuffer(_cipher);
        }

        public void Join()
        {
            var messageHeader = new MessageHeader
            {
                ChannelId = ChannelId,
                SessionId = SessionId,
                CommandId = Command.Join,
                Encrypted = true,
            };
            _outboundBuffer.Reset();
            _outboundBuffer.WriteHeader(messageHeader);
            _outboundBuffer.WriteInt(SessionId);
            _outboundBuffer.WriteUTF8(Presence.Token);
            _outboundBuffer.WriteUTF8(Presence.Ticket);
            var outData = _outboundBuffer.Drain();
            Send(outData,outData.Length);
            messageHeader.CommandId = Command.Ack;
            messageHeader.Encrypted = false;
            _ackOutboundBuffer.WriteHeader(messageHeader);
        }

        public void Send(MessageHeader messageHeader, Action<MessageBuffer> buffer)
        {
            _outboundBuffer.Reset();
            messageHeader.ChannelId = ChannelId;
            messageHeader.SessionId = SessionId;
            _outboundBuffer.WriteHeader(messageHeader);
            buffer(_outboundBuffer);    
            var outData = _outboundBuffer.Drain();
            if (messageHeader.Ack) _pendingAckMessage[messageHeader.ToString()] = new RetryData { Retries = Retries,Data = outData};
            Send(outData,outData.Length);
        }

        public void Leave()
        {
            var messageHeader = new MessageHeader
            {
                ChannelId = ChannelId,
                SessionId = SessionId,
                CommandId = Command.Leave
            };
            _outboundBuffer.Reset();
            _outboundBuffer.WriteHeader(messageHeader);
            _outboundBuffer.WriteInt(SessionId);
            var outData = _outboundBuffer.Drain();
            Send(outData,outData.Length);
            _ackOutboundBuffer.WriteHeader(messageHeader);
        }

        public void Ping()
        {
            if(!Joined) return;
            Send(_ping,_ping.Length);    
        }

        public void Retry()
        {
            if(!Joined) return;
            foreach (var keyValue in _pendingAckMessage)
            {
                var retry = keyValue.Value;
                retry.Retries--;
                Send(retry.Data,retry.Data.Length);
                if (retry.Retries <= 0)
                {
                    _pendingAckMessage.Remove(keyValue.Key);
                }
            }
        }

        private void Ack(MessageHeader messageHeader)
        {
            _ackOutboundBuffer.Reset(MessageBuffer.HeaderSize);
            for (var i = 1; i < AckSize; i++)
            {
                _ackOutboundBuffer.WriteHeader(_pendingAck[i]);
                _pendingAck[i - 1] = _pendingAck[i];
            }
            _pendingAck[AckSize-1] = messageHeader;
            _ackOutboundBuffer.WriteHeader(messageHeader);
            var acks = _ackOutboundBuffer.Drain();
            Send(acks,acks.Length);
        }

        private void Send(byte[] payload,int length)
        {
            _udpClient.BeginSend(payload,length, SendCallback,null);
        }

        private void SendCallback(IAsyncResult asyncResult)
        {
            _udpClient.EndSend(asyncResult);
        }
        
        private void ReceiveCallback(IAsyncResult asyncResult)
        {
            var ret = _udpClient.EndReceive(asyncResult, ref _ipEndPoint);
            _inboundBuffer.Reset(ret);
            var messageHeader = _inboundBuffer.ReadHeader();
            if (messageHeader.Ack)
            {
                Ack(messageHeader);
            }
            switch (messageHeader.CommandId)
            {
                case Command.OnJoin:
                    if (messageHeader.SessionId == SessionId)
                    {
                        messageHeader.CommandId = Command.Ping;
                        _inboundBuffer.Reset();
                        _inboundBuffer.WriteHeader(messageHeader);
                        _ping = _inboundBuffer.Drain();
                        Joined = true;
                    }
                    OnJoin?.Invoke(messageHeader.SessionId);
                    break;
                case Command.OnLeave:
                    break;
                case Command.OnRequest:
                    
                    OnRequest?.Invoke(messageHeader,ret);
                    break;
                case Command.Ack:
                    for (var i = 0; i < AckSize; i++)
                    {
                        var ack = _inboundBuffer.ReadHeader();
                        _pendingAckMessage.Remove(ack.ToString());
                    }
                    break;
                default:
                    OnMessage?.Invoke(messageHeader,ret);        
                    break;
            }
            _udpClient.BeginReceive(ReceiveCallback, null);
        }

        public void Close()
        {
            _udpClient.Close();
            _udpClient.Dispose();
        }
    }
}