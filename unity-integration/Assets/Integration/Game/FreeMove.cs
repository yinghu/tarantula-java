using GameClustering;
using UnityEngine;

namespace Integration.Game
{
    public class FreeMove : ClusteringObject
    {
        private  float _speed = 6f;
        private Vector3 _end;
        private float _timer;
        private float _step;
        private float _direction;
        private void Start()
        {
            OnSync( buffer =>
            {
                buffer.PutVector3(_end);
            },
            buffer =>
            {
                _end = buffer.GetVector3();
            });
            _timer = 0.5f;
            _step = 1f;
            _direction = -1f;
            _end = transform.position;
        }
        
        private  void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, _speed*Time.fixedDeltaTime);
            _timer -= Time.fixedDeltaTime;
            if (_timer > 0)
            {
                return;
            }
            _timer = 0.5f;
            _end.x += _step*_direction;
        }

        private async void OnTriggerEnter(Collider other)
        {
            if (!master||!other.gameObject.CompareTag("pvx"))
            {
                return;
            }
            _direction *= -1f;
            using (var buffer = new DataBuffer())
            {
                buffer.PutFloat(_direction);
                await Messenger.SendAsync(MessageType.Collision, sequence, true,buffer);
            }
        }
        
        public override void Setup(int oid, bool owner)
        {
            base.Setup(oid,owner);
            Messenger.RegisterMessageHandler(MessageType.Move,sequence, (sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    _end = buffer.GetVector3();
                    _speed = buffer.GetFloat();
                });
            });
            Messenger.RegisterMessageHandler(MessageType.OnCollision,sequence,  (sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    _direction = buffer.GetFloat();
                });
            });
        }
    }
}