using UnityEngine;

namespace Holee
{
    public class Replication : MonoBehaviour,IMessage
    {
        [SerializeField] private int networkingId;
        
        private float _timer;
        private bool _isTriggering;
        private int _sequence;
        private GameManager _gameManager;
        private Rigidbody _rigidbody;
        private MessageHeader _messageHeader;
        private void Start()
        {
            Debug.Log("Starting networking object->"+networkingId);
            _rigidbody = gameObject.GetComponent<Rigidbody>();
            _sequence = 0;
            _messageHeader = new MessageHeader
            {
                ObjectId = networkingId
            };
        }
        

        private void FixedUpdate()
        {
            if(!_isTriggering) return;
            _timer += Time.deltaTime;//20ms per frame 
            if (_timer < 0.1 || _rigidbody.IsSleeping()) return; 
            _timer = 0;
            _messageHeader.Sequence = ++_sequence;
            _gameManager.Send(_messageHeader, buffer => buffer.WriteVector3(_rigidbody.velocity).WriteVector3(_rigidbody.position).WriteVector3(_rigidbody.angularVelocity));
        }
        

        public void Setup(GameManager gameManager)
        {
            _gameManager = gameManager;
        }

        public void OnTrigger()
        {
            _isTriggering = true;
        }
        public void OnMessage(MessageHeader header,MessageBuffer messageBuffer)
        {
            if(header.Sequence<=_sequence) return;
            _isTriggering = false;
            _sequence = header.Sequence;
            _rigidbody.velocity = messageBuffer.ReadVector3();
            _rigidbody.position = messageBuffer.ReadVector3();
            _rigidbody.angularVelocity = messageBuffer.ReadVector3();
        }
    }
}