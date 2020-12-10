namespace GameClustering
{
    public static class MessageType
    {
        public const int Ack = 0;
        public const int Join = 1;
        public const int Leave = 2;
        public const int Ping = 3;
        public const int Pong = 4;
        public const int Sync = 5;
        public const int Move = 6;
        public const int Spawn = 7;
        public const int Collision = 8;
        public const int Destroy = 9;
        public const int Load = 10;
        public const int Action = 11;
        
        public const int OnJoined = 100;
        public const int OnLeft = 101;
        public const int OnKickedOff = 102;
        public const int OnAction = 103;
        public const int OnSync = 104;
        public const int OnCollision = 105;
        public const int OnSpawn = 106;
        
        public const int GameStart = 202;
        public const int GameClosing = 203;
        public const int GameClose = 204;
        public const int GameEnd = 205;

        public const int GameJoinTimeout = 305;
        public const int GameOvertime = 306;
    }
}