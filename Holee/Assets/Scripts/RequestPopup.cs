using System.Text;
using Newtonsoft.Json;
using UnityEngine;

namespace Holee
{
    public class RequestPopup : MonoBehaviour,IMessage
    {
        [SerializeField] private int networkingId;

        private GameManager _gameManager;
        private MessageHeader _messageHeader;
        private void Start()
        {
            Debug.Log("start popup");
            _messageHeader = new MessageHeader
            {
                CommandId = Command.Request,
                ObjectId = networkingId
            };
        }

        public void OnRequest()
        {
            _gameManager.Send(_messageHeader, buffer =>
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
        
        public void Setup(GameManager gameManager)
        {
            _gameManager = gameManager;
        }

    }
}