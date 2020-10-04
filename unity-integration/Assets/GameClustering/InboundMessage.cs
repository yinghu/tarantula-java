using System.IO;

namespace GameClustering
{
    public class InboundMessage
    {
        private readonly MemoryStream _memoryStream;
        public InboundMessage(byte[] buffer)
        {
            _memoryStream = new MemoryStream(buffer);
        }

        public bool Ack()
        {
            _memoryStream.ReadByte();
            return true;
        }

        public int Type()
        {
            return 1;
        }

        public int MessageId()
        {
            return 1;
        }

        public int Sequence()
        {
            return 1;
        }

        public long ConnectionId()
        {
            return 1;
        }

        public byte[] Message()
        {
            return null;
        }
        public void Close()
        {
            _memoryStream.Close();
        }
    }
}