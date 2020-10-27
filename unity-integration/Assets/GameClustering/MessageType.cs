namespace GameClustering
{
    public static class MessageType
    {
        public const int Ack = 0;
        public const int Join = 1;
        public const int Echo = 2;
        public const int Relay = 3;
        public const int Leave = 4;
        public const int Spawn = 5;
        public const int Ping = 6;
        public const int Pong = 7;
        public const int Vote = 8;
        public const int Sync = 9;
        public const int OnJoined = 10;
        public const int OnLeft = 11;
    }
}