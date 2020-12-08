namespace GameClustering
{
    public static class MessageType
    {
        public const int Ack = 0;
        public const int Join = 1;
        public const int Move = 2;
        public const int Leave = 3;
        public const int Spawn = 4;
        public const int Ping = 5;
        public const int Pong = 6;
        public const int Collision = 7;
        public const int Sync = 8;
        public const int Action = 9;
        public const int Destroy = 10;
        
        public const int OnJoined = 100;
        public const int OnLeft = 101;
        public const int OnKickedOff = 102;
        public const int OnAction = 103;
        public const int OnSync = 104;
        
        public const int GameStart = 202;
        public const int GameClosing = 203;
        public const int GameClose = 204;
        public const int GameEnd = 205;

        public const int GameJoinTimeout = 305;
        public const int GameOvertime = 306;
    }
}