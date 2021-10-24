using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Security.Cryptography;
using UnityEngine;

namespace Holee
{
    public class GameManager : MonoBehaviour,IMessage
    {
       
        public Player playerA;
        public Player playerB;
        [SerializeField] private Replication[] replications;
        private ConcurrentQueue<byte[]> _messageQueue;
        private Dictionary<string, byte[]> _pendingAckMessage;
        private MessageHeader[] _pendingAck;
        private MessageBuffer _outboundBuffer;
        private MessageBuffer _inboundBuffer;
        private MessageBuffer _ackOutboundBuffer;
        private Rijndael _cipher;
        private MessageHeader _header;
        private float _timer;
        private void Start()
        {
            _pendingAckMessage = new Dictionary<string, byte[]>();
            _pendingAck = new MessageHeader[10];
            for (var i = 0; i < 10; i++)
            {
                _pendingAck[i] = new MessageHeader();
            }

            foreach (var r in replications)
            {
                r.Setup(this);    
            }
            _cipher = new RijndaelManaged
            {
                //Key = serverKey,
                Padding = PaddingMode.PKCS7,
                Mode = CipherMode.CBC,
                //IV = serverKey
            };
            _cipher.GenerateKey();
            _cipher.GenerateIV();
            _messageQueue = new ConcurrentQueue<byte[]>();
            _outboundBuffer = new MessageBuffer(_cipher);
            _inboundBuffer = new MessageBuffer(_cipher);
            _ackOutboundBuffer = new MessageBuffer(_cipher);
            _header = new MessageHeader
            {
                ChannelId = 1,
                SessionId = 2,
                ObjectId = 0,
                CommandId = Command.Ack
            };
            _ackOutboundBuffer.WriteHeader(_header);
            _header.CommandId = Command.Join;
            NetworkingManager.OnReceived += OnMessage;
            _outboundBuffer.WriteHeader(_header);
            _outboundBuffer.WriteInt(2);
            var outbound = _outboundBuffer.Drain();
            _pendingAckMessage[_header.ToString()] = outbound;
            NetworkingManager.Send(outbound,outbound.Length);
        }

        public void OnPlayA()
        {
            playerB.OffPlay();
            playerA.OnPlay();
        }
        
        public void OnPlayB()
        {
            playerA.OffPlay();
            playerB.OnPlay();
        }
        
        public void Send(MessageHeader header,Action<MessageBuffer> message)
        {
            header.ChannelId = 1;
            header.SessionId = 2;
            message(_outboundBuffer.WriteHeader(header));
            var outbound = _outboundBuffer.Drain();
            if (header.Ack)
            {
                _pendingAckMessage[header.ToString()] = outbound;
            }

            NetworkingManager.Send(outbound,outbound.Length);
        }

        private void OnMessage(byte[] message)
        {
            _messageQueue.Enqueue(message);
        }

        private void FixedUpdate()
        {
            _timer += Time.deltaTime;//20ms per frame 
            if (_timer >= 1)
            {
                _timer = 0;
                _header.Ack = false;
                _header.CommandId = Command.Ping;
                _outboundBuffer.WriteHeader(_header);
                var ping = _outboundBuffer.Drain();
                NetworkingManager.Send(ping, ping.Length);
            }

            var suc = _messageQueue.TryDequeue(out var message);
            if(!suc) return;
            _inboundBuffer.Reset(message);
            var header = _inboundBuffer.ReadHeader();
            if (header.CommandId == Command.Ack)
            {
                //Debug.Log("ACK->"+header);
                for (var i = 0; i < 10; i++)
                {
                    var ack = _inboundBuffer.ReadHeader();
                    if (_pendingAckMessage.ContainsKey(ack.ToString()))
                    {
                        Debug.Log("ACK->"+i+">>>"+ack+" removed->"+_pendingAckMessage.Remove(ack.ToString()));
                    }
                }

                return;
            }
            if (header.Ack)
            {
                _ackOutboundBuffer.Reset(MessageBuffer.HeaderSize);
                for (var i = 1; i < 10; i++)
                {
                    _ackOutboundBuffer.WriteHeader(_pendingAck[i]);
                    _pendingAck[i - 1] = _pendingAck[i];
                }
                _pendingAck[9] = header;
                _ackOutboundBuffer.WriteHeader(header);
                var acks = _ackOutboundBuffer.Drain();
                NetworkingManager.Send(acks,acks.Length);
            }
            if (header.CommandId == Command.OnJoin)
            {
                Debug.Log("ON JOIN->" + header);
                header.Sequence = 0;
                if (_pendingAckMessage.ContainsKey(header.ToString()))
                {
                    Debug.Log("ACK removed->" + _pendingAckMessage.Remove(header.ToString()));
                }

                return;
            }

            switch (header.ObjectId)
            {
                case 0:
                    replications[0].OnMessage(header,_inboundBuffer);
                    //Debug.Log("MSG->"+header.ChannelId+"<>"+header.SessionId+"<>"+header.CommandId);
                    break;
                case 1:
                    playerA.OnMessage(header,_inboundBuffer);
                    break;
                case 2:
                    playerB.OnMessage(header,_inboundBuffer);
                    break;
                case 3:
                    replications[1].OnMessage(header,_inboundBuffer);
                    break;
                case 4:
                    replications[0].OnMessage(header,_inboundBuffer);
                    break;
            }
        }

        private void OnDestroy()
        {
            NetworkingManager.Close();
            _outboundBuffer.Dispose();
            _inboundBuffer.Dispose();
            NetworkingManager.OnReceived -= OnMessage;
        }

        public void OnMessage(MessageHeader header, MessageBuffer messageBuffer)
        {
            
        }
    }
}