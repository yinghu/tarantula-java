namespace GameClustering
{
    public class Connection
    {
        public string Type { set; get; }
        public long ConnectionId { set; get; }
        public int Sequence { set; get; }
        public string Host { set; get; }
        public int Port { set; get; }
    }
}