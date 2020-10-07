using System;
using System.IO;

namespace GameClustering
{
    public class InboundMessage
    {
        public const int SequenceSize = 16;
        public const long AckPos = 0;
        public const long TypePos = 1;
        public const long MessageIdPos = 5;
        public const long ConnectionIdPos = 9;
        public const long SequencePos = 17;
        public const long PayloadPos = SequencePos+SequenceSize;
        
        private readonly MemoryStream _memoryStream;
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

        public byte[] Sequence()
        {
            _memoryStream.Position = SequencePos;
            var sequence = new byte[SequenceSize];
            _memoryStream.Read(sequence, 0, SequenceSize);
            return sequence;
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
        public void Close()
        {
            _memoryStream.Close();
        }
    }
}