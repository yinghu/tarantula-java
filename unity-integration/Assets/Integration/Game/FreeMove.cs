using System.Collections;
using System.Threading.Tasks;
using GameClustering;
using UnityEngine;

namespace Integration.Game
{
    public class FreeMove : ClusteringObject
    {
        private  const float Speed = 3f;
        private Vector3 _end;
        private float _timer;
        private float _step;
        private float _direction;
        private Board _board;
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
            _board = GameObject.Find("/Board").GetComponent<Board>();
            _timer = 1f;
            _step = 1f;
            _direction = -1f;
            _end = transform.position;
        }
        
        private async void FixedUpdate()
        {
            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);
            if (!master)
            {
                return;
            }
            _timer -= Time.fixedDeltaTime;
            if (_timer > 0)
            {
                return;
            }
            _timer = 1f;
            _end.x += _step*_direction;
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(_end);
                await Messenger.SendAsync(MessageType.OnSync, sequence, true, buffer);
            }
        }

        private async void OnTriggerEnter(Collider other)
        {
            if (!other.gameObject.CompareTag("pvx"))
            {
                return;
            }
            _direction *= -1f;
            if (!master)
            {
                return;
            }
            _board.Remove(sequence);
            StartCoroutine(Kill());
        }
        
        public override void Setup(int oid, bool owner)
        {
            base.Setup(oid,owner);
            Messenger.RegisterMessageHandler(MessageType.Destroy,sequence,  (sessionId, data) =>
            {
                MainThread.Execute(data,buffer =>
                {
                    DestroyImmediate(gameObject);
                });
            });
        }

        private IEnumerator Kill()
        {
            Task.FromResult(Messenger.SendAsync(MessageType.Destroy, sequence, true));
            yield return new WaitForSeconds(0.1f);
            Destroy(gameObject);
        }
    }
}