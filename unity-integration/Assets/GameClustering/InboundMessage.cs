using System;
using System.IO;

namespace GameClustering
{
    public class InboundMessage : IDisposable
    {
        public const long AckPos = 0;
        public const long TypePos = 1;
        public const long MessageIdPos = 5;
        public const long ConnectionIdPos = 9;
        public const long SequencePos = 13;
        public const long SessionIdPos = 17;
        public const long TimestampPos = 21;
        public const long PayloadPos = 29;
        
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
        public int SessionId()
        {
            _memoryStream.Position = SessionIdPos;
            var  sessionId = new byte[4];
            _memoryStream.Read(sessionId, 0, 4);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(sessionId);
            }
            return BitConverter.ToInt32(sessionId, 0);
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

        public int ConnectionId()
        {
            _memoryStream.Position = ConnectionIdPos;
            var connectionId = new byte[4];
            _memoryStream.Read(connectionId, 0, 4);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(connectionId);
            }
            return BitConverter.ToInt32(connectionId, 0);
        }
        public long Timestamp()
        {
            _memoryStream.Position = TimestampPos;
            var timestamp = new byte[8];
            _memoryStream.Read(timestamp, 0, 8);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(timestamp);
            }
            return BitConverter.ToInt64(timestamp, 0);
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