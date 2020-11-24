using System.Threading.Tasks;
using GameClustering;
using UnityEngine;

namespace Integration.Game
{
    public class FreeMove : MonoBehaviour
    {
        private int _sequence;
        private bool _master;
        private Vector3 _target;
        private const float Speed = 3f;
        private Vector3 _end;
       
        private IntegrationManager _integrationManager;
        private float _timer;
        private float _delta;
        private void Start()
        {
            _timer = 0.5f;
            _delta = 1;
            _target = transform.position;
            _end = _target;
            _integrationManager = IntegrationManager.Instance;
           
        }

        private async Task Move(Vector3 target)
        {
            //_end = target;
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(target);
                await _integrationManager.Messenger.SendAsync(MessageType.Relay, _sequence, true, buffer.ToArray());
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
            if (!_master)
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
        
        public void Setup(int sequence, bool master)
        {
            _sequence = sequence;
            _master = master;
            IntegrationManager.Instance.Messenger.RegisterMessageHandler(MessageType.Relay,_sequence, (sessionId, data) =>
            {
                MessageContext.Instance.Execute(data, buffer =>
                {
                    _end = buffer.GetVector3();
                });
            });
        }
    }
}