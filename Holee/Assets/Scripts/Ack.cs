using UnityEngine;

namespace Holee
{
    public class Ack : MonoBehaviour
    {
        private MessageHeader[] _pendingAck;
        private MessageBuffer _ackOutboundBuffer;
        private void Start()
        {
            Debug.Log("Start Ack");
            _pendingAck = new MessageHeader[10];
            for (var i = 0; i < 10; i++)
            {
                _pendingAck[i] = new MessageHeader();
            }
            _ackOutboundBuffer = new MessageBuffer();
        }

        public void OnJoin(MessageHeader header)
        {
            header.CommandId = Command.Ack;
            _ackOutboundBuffer.WriteHeader(header);
        }

        public void OnAck(MessageHeader messageHeader)
        {
            _ackOutboundBuffer.Reset(MessageBuffer.HeaderSize);
            for (var i = 1; i < 10; i++)
            {
                _ackOutboundBuffer.WriteHeader(_pendingAck[i]);
                _pendingAck[i - 1] = _pendingAck[i];
            }
            _pendingAck[9] = messageHeader;
            _ackOutboundBuffer.WriteHeader(messageHeader);
            var acks = _ackOutboundBuffer.Drain();
            NetworkingManager.Send(acks,acks.Length);
        }

    }
}