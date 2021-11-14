using System;
using System.Collections.Concurrent;
using UnityEngine;

namespace Holee
{
    public class GameManager : MonoBehaviour,IMessage
    {
        
        public Player playerA;
        public Player playerB;
        [SerializeField] private Replication[] replications;
       
        [SerializeField] private RequestPopup requestPopup;
        private ConcurrentQueue<PendingMessage> _messageQueue;
        
        private Channel _channel;
        private bool _started;
        private float _timerOnPing;
        private float _timerOnRetry;

        public GameClusterManager GameClusterManager { get; private set; }

        private void Start()
        {
            GameClusterManager = new GameClusterManager();
            foreach (var r in replications)
            {
                r.Setup(this);    
            }
            _messageQueue = new ConcurrentQueue<PendingMessage>();
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
            if (GameClusterManager.Room.Seat == 1)
            {
                playerA.OffPlay();
            }
            else{
                playerB.OffPlay();
            }
            await GameClusterManager.Leave(this);
            _channel.Leave();
            GameClusterManager.Channel.Leave();
            Debug.Log("disconnected->"+_channel.ChannelId);
        }

        public async void OnDevice()
        {
            if(!await GameClusterManager.Device(this)) return;
            if (!await GameClusterManager.Join(this)) return;
            _channel = GameClusterManager.Room.Channel;
            _channel.Init();
            _channel.OnMessage += OnMessage;
            _channel.OnJoin += OnJoin;
            _channel.Join();
            requestPopup.Setup(this);
        }

        public void Send(MessageHeader header,Action<MessageBuffer> message)
        {
            _channel.Send(header,message);
        }
        
        private void FixedUpdate()
        {
            if(!_started) return;
            _timerOnPing += Time.deltaTime;//20ms per frame
            _timerOnRetry += Time.deltaTime;
            if (_timerOnRetry >= 0.2)
            {
                _channel.Retry();
                _timerOnRetry = 0;
            }

            if (_timerOnPing >= 1)
            { 
                _channel.Ping();
                _timerOnPing = 0;
            }

            var suc = _messageQueue.TryDequeue(out var message);
            if(!suc) return;
            var header = message.MessageHeader;
            switch (header.ObjectId)
            {
                case 0:
                    replications[0].OnMessage(header,message.MessageBuffer);
                    //Debug.Log("MSG->"+header.ChannelId+"<>"+header.SessionId+"<>"+header.CommandId);
                    break;
                case 1:
                    playerA.OnMessage(header,message.MessageBuffer);
                    break;
                case 2:
                    playerB.OnMessage(header,message.MessageBuffer);
                    break;
                case 3:
                    replications[1].OnMessage(header,message.MessageBuffer);
                    break;
                case 4:
                    replications[0].OnMessage(header,message.MessageBuffer);
                    break;
            }
        }

        private void OnDestroy()
        {
            _channel.Close();
            GameClusterManager.Channel.Close();
        }

        public void OnJoin(int sessionId)
        {
            _started = true;
            Debug.Log("Session joined->"+sessionId);
            playerA.OnPlay();
            //if (GameClusterManager.Room.Seat == 1)
            //{
                //OnPlayA();
            //}
            //else
            //{ 
                //OnPlayB();
            //}
        }

        public void OnMessage(MessageHeader header, MessageBuffer messageBuffer)
        {
            _messageQueue.Enqueue(new PendingMessage
            {
                MessageHeader = header,
                MessageBuffer = messageBuffer
            });             
        }
    }
}