using System;
using System.IO;

namespace GameClustering
{
    public class OutboundMessage
    {
        public const int MessageSize = 512;
        
        private readonly MemoryStream _memoryStream;
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
        
        public void Sequence(byte[] sequence)
        {
            _memoryStream.Position = InboundMessage.SequencePos;
            _memoryStream.Write(sequence,0,48);
        }
        
        public void Payload(byte[] payload)
        {
            _memoryStream.Position = InboundMessage.PayloadPos;
            _memoryStream.Write(payload,0,payload.Length);
        }
        public byte[] Message()
        {
            return _memoryStream.ToArray();
        }
        public void Close()
        {
            _memoryStream.Close();
        }
    }
}