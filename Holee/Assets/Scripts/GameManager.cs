using System;
using System.Collections.Concurrent;
using System.Security.Cryptography;
using UnityEngine;

namespace Holee
{
    public class GameManager : MonoBehaviour,IMessage
    {
        private GameClusterManager _gameClusterManager;
        public Player playerA;
        public Player playerB;
        [SerializeField] private Replication[] replications;
        [SerializeField] private Ping ping;
        [SerializeField] private Retry retry;
        [SerializeField] private Ack ack;
        [SerializeField] private RequestPopup requestPopup;
        private ConcurrentQueue<byte[]> _messageQueue;
     
        private MessageBuffer _outboundBuffer;
        private MessageBuffer _inboundBuffer;
        private Rijndael _cipher;
        private MessageHeader _header;
        private Channel _channel;

        private void Start()
        {
            _gameClusterManager = new GameClusterManager();
            foreach (var r in replications)
            {
                r.Setup(this);    
            }
            requestPopup.Setup(this);
            _messageQueue = new ConcurrentQueue<byte[]>();
        }
        private void OnPlay()
        {
            var key = _channel.ServerKey;
            _cipher = new RijndaelManaged
            {
                Key = key,
                Padding = PaddingMode.PKCS7,
                Mode = CipherMode.CBC,
                IV = key
            };
            NetworkingManager.Init(_channel.Host,_channel.Port);
            _outboundBuffer = new MessageBuffer(_cipher);
            _inboundBuffer = new MessageBuffer(_cipher);
            _header = new MessageHeader
            {
                ChannelId = _channel.ChannelId,
                SessionId = _channel.SessionId,
                ObjectId = 0,
                Sequence = 1,
                CommandId = Command.Ack
            };
            ack.OnJoin(_header);
            NetworkingManager.OnReceived += OnMessage;
            _outboundBuffer.WriteHeader(new MessageHeader
            {
                ChannelId = _channel.ChannelId,
                SessionId = _channel.SessionId,
                ObjectId = 0,
                Sequence = 2,
                CommandId = Command.Join,
                Encrypted = true
            });
            _outboundBuffer.WriteInt(_channel.SessionId);
            _outboundBuffer.WriteUTF8(_gameClusterManager.Presence.Token);
            _outboundBuffer.WriteUTF8(_gameClusterManager.Ticket);
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
        
        public async void OnLeave()
        {
            if (_gameClusterManager.Seat == 1)
            {
                playerA.OffPlay();
            }
            else{
                playerB.OffPlay();
            }
            await _gameClusterManager.Leave(this);
        }

        public async void OnDevice()
        {
            if(!await _gameClusterManager.Device(this)) return;
            if (!await _gameClusterManager.Join(this)) return;
            _channel = _gameClusterManager.Channel;
            Debug.Log("CHANNEL ID->"+_channel.ChannelId);
            Debug.Log("SESSION ID->"+_channel.SessionId);
            OnPlay();
            if (_gameClusterManager.Seat == 1)
            {
                OnPlayA();
            }
            else
            { 
                OnPlayB();
            }
        }

        public void Send(MessageHeader header,Action<MessageBuffer> message)
        {
            header.ChannelId = _channel.ChannelId;
            header.SessionId = _channel.SessionId;
            message(_outboundBuffer.WriteHeader(header));
            var outbound = _outboundBuffer.Drain();
            if (header.Ack)
            {
                retry.PendingAck(header.ToString(),outbound,3);
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
            Debug.Log(message.Length+">>>>>"+header.CommandId);
            if (header.CommandId == Command.Ack)
            {
                retry.OnAck(_inboundBuffer);
                return;     
            }
            if (header.Ack)
            {
                ack.OnAck(header);
            }
            if (header.CommandId == Command.OnJoin)
            {
                Debug.Log("On JOINED->" + header);
                if(header.SessionId != _channel.SessionId) return;
                header.CommandId = Command.Ping;
                _outboundBuffer.Reset();
                _outboundBuffer.WriteHeader(header);
                var data = _outboundBuffer.Drain();
                ping.OnJoin(data);
                return;
            }

            if (header.CommandId == Command.OnRequest)
            {
                requestPopup.OnMessage(header,_inboundBuffer);
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