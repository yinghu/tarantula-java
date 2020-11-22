using System.Collections;
using System.Collections.Concurrent;
using System.Threading.Tasks;
using GameClustering;
using UnityEngine;

namespace Integration.Game
{
    public class FreeMove : MonoBehaviour
    {
        public int sequence = 3;
        private Vector3 _target;
        private const float Speed = 3f;
        private Vector3 _end;
        private ConcurrentQueue<Vector3> _queue;
        private IntegrationManager _integrationManager;
        private float _timer;
        private float _delta;
        public GameObject bullet;
        private void Start()
        {
            _timer = 0.5f;
            _delta = 1;
            _target = transform.position;
            _end = _target;
            _queue = new ConcurrentQueue<Vector3>();
            _integrationManager = IntegrationManager.Instance;
            _integrationManager.Messenger.RegisterMessageHandler(MessageType.Relay,sequence, (sessionId, data) =>
            {
                using (var buffer = new DataBuffer(data))
                {
                    _queue.Enqueue(buffer.GetVector3());
                }
            });
        }

        private async Task Move(Vector3 target)
        {
            //_end = target;
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(target);
                await _integrationManager.Messenger.SendAsync(MessageType.Relay, sequence, true, buffer.ToArray());
            }
        }

        private void Update()
        {
            if (!_queue.TryDequeue(out var pos))
            {
                return;
            }
            _end = pos;
            StartCoroutine(FireBullet());
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
            var cur = transform.position;
            var left = new Vector3(cur.x+(_delta),cur.y,cur.z);
            await Move(left);
        }

        private void OnTriggerEnter(Collider other)
        {
            if (other.gameObject.CompareTag("pvx"))
            {
                _delta *= -1;
            }
        }

        private IEnumerator FireBullet()
        {
            var shot = Instantiate(bullet,transform.position, Quaternion.identity);
            yield return new WaitForSeconds(1);
            Destroy(shot);
        }
    }
}