using System.Text;
using Newtonsoft.Json;
using UnityEngine;

namespace Holee
{
    public class RequestPopup : MonoBehaviour,IMessage
    {
        [SerializeField] private int networkingId;

        private Channel _channel;
        private MessageHeader _messageHeader;
        private float _timerOnPing;
        private float _timerOnRetry;
        private bool _started;
        private void Start()
        {
            Debug.Log("start popup");
            _messageHeader = new MessageHeader
            {
                CommandId = Command.Request,
                ObjectId = networkingId
            };
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
            if (_timerOnPing < 1) return;
            _channel.Ping();
            _timerOnPing = 0;
        }
        public void OnRequest()
        {
            _channel.Send(_messageHeader, buffer =>
            {
                buffer.WriteUTF8(JsonConvert.SerializeObject(_messageHeader));
            });
        }

        public void OnMessage(MessageHeader header, MessageBuffer messageBuffer)
        {
            var payload = messageBuffer.ReadPayload();
            Debug.Log(header+">>"+header.Batch+" of "+header.BatchSize);
            Debug.Log(Encoding.UTF8.GetString(payload));
        }

        public void OnJoin(int sessionId)
        {
            _started = true;
            Debug.Log("Session joined on popup->"+sessionId);
        }

        public void Setup(GameManager gameManager)
        {
            Debug.Log("POPUP SETUP");
            _channel = gameManager.GameClusterManager.Channel;
            _channel.Init();
            _channel.OnRequest += OnMessage;
            _channel.OnJoin += OnJoin;
            _channel.Join();
        }

    }
}