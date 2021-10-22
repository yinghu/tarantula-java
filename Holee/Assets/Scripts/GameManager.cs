using System;
using System.Collections.Concurrent;
using System.Security.Cryptography;
using UnityEngine;

namespace Holee
{
    public class GameManager : MonoBehaviour
    {
       
        public Player playerA;
        public Player playerB;
        [SerializeField] private Replication[] replications;
        private ConcurrentQueue<byte[]> _messageQueue;
       
        private MessageBuffer _outboundBuffer;
        private MessageBuffer _inboundBuffer;
        private Rijndael _cipher;
        private MessageHeader _header;
        private void Start()
        {
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
            _header = new MessageHeader
            {
                Ack = true,
                ChannelId = 1,
                SessionId = 2,
                ObjectId = 0,
                CommandId = Command.Join
            };
            NetworkingManager.OnReceived += OnMessage;
            _outboundBuffer.WriteHeader(_header);
            _outboundBuffer.WriteInt(2);
            var outbound = _outboundBuffer.Drain();
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
                
            }

            NetworkingManager.Send(outbound,outbound.Length);
        }

        private void OnMessage(byte[] message)
        {
            _messageQueue.Enqueue(message);
        }

        private void FixedUpdate()
        {
            var suc = _messageQueue.TryDequeue(out var message);
            if(!suc) return;
            _inboundBuffer.Reset(message);
            var header = _inboundBuffer.ReadHeader();
            if (header.CommandId == Command.Ack)
            {
                Debug.Log("ACK->"+header.ChannelId+"<>"+header.SessionId+"<>"+header.CommandId);    
                return;
            }
            switch (header.ObjectId)
            {
                case 0:
                    //replications[0].OnMessage(header,_inboundBuffer);
                    Debug.Log("MSG->"+header.ChannelId+"<>"+header.SessionId+"<>"+header.CommandId);
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
    }
}