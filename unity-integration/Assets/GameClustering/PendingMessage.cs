namespace GameClustering
{
    public class PendingMessage
    {
        public byte[] Data { set; get; }
        public long Timestamp { set; get; }
        public int Retries { set; get; }

        public bool Inbound { set; get; }
    }
}