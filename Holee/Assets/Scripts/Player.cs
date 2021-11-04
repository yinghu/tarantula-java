using UnityEngine;

namespace Holee
{
    public class Player : MonoBehaviour,IMessage
    {
        public Camera playerCamera;
        public float speed = 10f;
        public int playerId;
        public GameManager gameManager;
        private bool _isMoving;
        private Vector3 _targetPos;
        private float _timer;
        private bool _isPlaying;
        private int _sequence;

        private MessageHeader _messageHeader;

        private void Start()
        {
            Debug.Log("starting game");
            _targetPos = transform.position;
            _sequence = 0;
            _messageHeader = new MessageHeader
            {
                ObjectId = playerId,
                CommandId = Command.Replication,
            };
        }

        private void Update()
        {
            if (!_isPlaying)
            {
                MoveObject();
                return;
            }
            if (Input.GetMouseButton(0)) SetTargetPosition();
            if (_isMoving) MoveObject();
        }

        private void SetTargetPosition()
        {
            var plane = new Plane(Vector3.up, transform.position);
            var ray = playerCamera.ScreenPointToRay(Input.mousePosition);
            if (plane.Raycast(ray, out var point)) _targetPos = ray.GetPoint(point);
            _isMoving = true;
        }

        private void FixedUpdate()
        {
            if(!_isPlaying) return;
            _timer += Time.deltaTime;//20ms per frame 
            if (!_isMoving || _timer < 0.1) return; 
            _timer = 0;
            _messageHeader.Sequence = ++_sequence;
            gameManager.Send(_messageHeader, buffer =>
            {
                buffer.WriteVector3(transform.position);
            });
        }

        private void MoveObject()
        {
            transform.LookAt(_targetPos);
            transform.position = Vector3.MoveTowards(transform.position, _targetPos, speed * Time.deltaTime);
            if (transform.position == _targetPos) _isMoving = false;
        }

        public void OnPlay()
        {
            _isPlaying = true;
        }
        public void OffPlay()
        {
            _isPlaying = false;
            _targetPos = transform.position;
        }
        
        public void OnMessage(MessageHeader header,MessageBuffer messageBuffer)
        {
            if(header.Sequence<=_sequence) return;
            _sequence = header.Sequence;
            _targetPos = messageBuffer.ReadVector3();
        }

    }
}
