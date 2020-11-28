using System.Threading.Tasks;
using GameClustering;
using UnityEngine;

namespace Integration.Game
{
    public class FreeMove : ClusteringObject
    {
        
        private Vector3 _target;
        private const float Speed = 6f;
        private Vector3 _end;
        
        private float _timer;
        private float _delta;
        private void Start()
        {
            _timer = 0.5f;
            _delta = 1;
            _target = transform.position;
            _end = _target;
           
        }

        private async Task Move(Vector3 target)
        {
            //_end = target;
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(target);
                await Messenger.SendAsync(MessageType.Relay, sequence, true, buffer.ToArray());
            }
        }
        private async void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);
            _timer -= Time.fixedDeltaTime;
            if (_timer > 0)
            {
                return;
            }
            _timer = 0.5f;
            if (!master)
            {
                return;
            }
            var cur = transform.position;
            var left = new Vector3(cur.x + (_delta), cur.y, cur.z);
            await Move(left);
        }

        private void OnTriggerEnter(Collider other)
        {
            if (other.gameObject.CompareTag("pvx"))
            {
                _delta *= -1;
            }
        }
        
        public override void Setup(int oid, bool owner)
        {
            base.Setup(oid,owner);
            Messenger.RegisterMessageHandler(MessageType.Relay,sequence, (sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    _end = buffer.GetVector3();
                });
            });
        }
    }
}