namespace GameClustering
{
    public interface IMessageHandler
    {
        void Handle(InboundMessage inboundMessage);
    }
}