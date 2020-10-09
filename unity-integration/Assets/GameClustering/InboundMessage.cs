using System;
using System.IO;
using UnityEngine;

namespace GameClustering
{
    public class InboundMessage : IDisposable
    {
        //public const int SequenceSize = 16;
        public const long AckPos = 0;
        public const long TypePos = 1;
        public const long MessageIdPos = 5;
        public const long ConnectionIdPos = 9;
        public const long SequencePos = 17;
        public const long PayloadPos = 21;//SequencePos+SequenceSize;
        
        private readonly MemoryStream _memoryStream;
        private bool _disposed;
        public InboundMessage(byte[] buffer)
        {
            _memoryStream = new MemoryStream(buffer);
        }

        public bool Ack()
        {
            _memoryStream.Position = AckPos;
            return _memoryStream.ReadByte() == 1;
        }

        public int Type()
        {
            _memoryStream.Position = TypePos;
            var type = new byte[4];
            _memoryStream.Read(type, 0, 4);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(type);
            }
            return BitConverter.ToInt32(type, 0);
        }

        public int MessageId()
        {
            _memoryStream.Position = MessageIdPos;
            var  messageId = new byte[4];
            _memoryStream.Read(messageId, 0, 4);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(messageId);
            }
            return BitConverter.ToInt32(messageId, 0);
        }

        public int Sequence()
        {
            _memoryStream.Position = SequencePos;
            var  sequence = new byte[4];
            _memoryStream.Read(sequence, 0, 4);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(sequence);
            }
            return BitConverter.ToInt32(sequence, 0);
        }

        public long ConnectionId()
        {
            _memoryStream.Position = ConnectionIdPos;
            var connectionId = new byte[8];
            _memoryStream.Read(connectionId, 0, 8);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(connectionId);
            }
            return BitConverter.ToInt64(connectionId, 0);
        }

        public byte[] Payload()
        {
            _memoryStream.Position = PayloadPos;
            var payload = new byte[OutboundMessage.MessageSize-PayloadPos];
            _memoryStream.Read(payload, 0, payload.Length);
            return payload;
        }

        public void Dispose() => Dispose(true);
        protected virtual void Dispose(bool disposing)
        {
            Debug.Log("release resource 1");
            if (_disposed)
            {
                return;
            }
            if (disposing)
            {
                _memoryStream?.Dispose();
            }
            _disposed = true;
        }
        ~InboundMessage() => Dispose(false);
    }
}