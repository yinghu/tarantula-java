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
            NetworkingManager.OnReceived += OnMessage;
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
            switch (header.ObjectId)
            {
                case 0:
                    replications[0].OnMessage(header,_inboundBuffer);
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