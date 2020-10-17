namespace GameClustering
{
    public class Connection
    {
        public string Type { set; get; }
        public long ConnectionId { set; get; }
        public int SessionId { set; get; }
        public string Host { set; get; }
        public int Port { set; get; }
        public bool Secured { set; get; }
    }
}