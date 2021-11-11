using System;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using UnityEngine;

namespace Holee
{
    public delegate void OnMessage(MessageHeader messageHeader,MessageBuffer message);
    public class Channel
    {
        public event OnMessage OnMessage;
        
        private  readonly UdpClient _udpClient;
        private IPEndPoint _ipEndPoint;
        private Rijndael _cipher;
        private MessageBuffer _outboundBuffer;
        private MessageBuffer _inboundBuffer;
        
        public int ChannelId { set; get; }
        public int SessionId { set; get; }

        public byte[] ServerKey { set; get; }
        public string Host { set; get; }
        public int Port { set; get; }

        public Channel()
        {
            _udpClient = new UdpClient();
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

        public void Send(MessageHeader messageHeader, Action<MessageBuffer> buffer)
        {
            _outboundBuffer.Reset();
            messageHeader.ChannelId = ChannelId;
            messageHeader.SessionId = SessionId;
            _outboundBuffer.WriteHeader(messageHeader);
            buffer(_outboundBuffer);
            var outData = _outboundBuffer.Drain();
            Send(outData,outData.Length);
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
            OnMessage?.Invoke(messageHeader,_inboundBuffer);
            _udpClient.BeginReceive(ReceiveCallback, null);
        }
    }
}