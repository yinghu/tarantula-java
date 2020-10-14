using System;
using System.IO;

namespace GameClustering
{
    public class OutboundMessage : IDisposable
    {
        public const int MessageSize = 512;
        
        private readonly MemoryStream _memoryStream;
        private bool _disposed;
        private int _payloadSize;
        public OutboundMessage(){
            _memoryStream = new MemoryStream(new byte[MessageSize]);
        }
        
        public void Ack(bool ack)
        {
            _memoryStream.Position = InboundMessage.AckPos;
            _memoryStream.WriteByte(ack?(byte)1:(byte)0);
        }
        
        public void Type(int type)
        {
            var bytes = BitConverter.GetBytes(type);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            _memoryStream.Position = InboundMessage.TypePos;
            _memoryStream.Write(bytes,0,4);
        }
        
        public void MessageId(int messageId)
        {
            var bytes = BitConverter.GetBytes(messageId);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            _memoryStream.Position = InboundMessage.MessageIdPos;
            _memoryStream.Write(bytes,0,4);
        }
        
        public void ConnectionId(long connectionId)
        {
            var bytes = BitConverter.GetBytes(connectionId);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            _memoryStream.Position = InboundMessage.ConnectionIdPos;
            _memoryStream.Write(bytes,0,8);
        }
        
        public void Sequence(int sequence)
        {
            var bytes = BitConverter.GetBytes(sequence);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            _memoryStream.Position = InboundMessage.SequencePos;
            _memoryStream.Write(bytes,0,4);
        }
        public void Timestamp(long timestamp)
        {
            var bytes = BitConverter.GetBytes(timestamp);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }
            _memoryStream.Position = InboundMessage.TimestampPos;
            _memoryStream.Write(bytes,0,8);
        }
        public void Payload(byte[] payload)
        {
            _memoryStream.Position = InboundMessage.PayloadPos;
            _memoryStream.Write(payload,0,payload.Length);
            _payloadSize = payload.Length;
        }
        public byte[] Message()
        {
            var payload = new byte[InboundMessage.PayloadPos + _payloadSize];
            _memoryStream.Position = InboundMessage.AckPos;
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
        ~OutboundMessage() => Dispose(false);
    }
}