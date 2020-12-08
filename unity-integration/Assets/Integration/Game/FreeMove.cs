using System.Threading.Tasks;
using GameClustering;
using UnityEngine;

namespace Integration.Game
{
    public class FreeMove : ClusteringObject
    {
        private const float Speed = 6f;
        private Vector3 _end;
        
        private float _timer;
        private float _delta;
        private Board _board;
        private bool _sent;
        private void Start()
        {
            _sent = false;
            _timer = 0.5f;
            _delta = Random.Range(-10, 10)>=0?2:-2;
            _end = transform.position;
            _board = FindObjectOfType<Board>();
        }

        private async Task Move(Vector3 target)
        {
            using (var buffer = new DataBuffer())
            {
                _sent = true;
                buffer.PutVector3(target);
                await Messenger.SendAsync(MessageType.Move, sequence, true, buffer.ToArray());
                await Messenger.SendAsync(MessageType.Action, sequence, true);
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
            if (!master||_sent)
            {
                return;
            }
            var cur = transform.position;
            var left = new Vector3(cur.x + _delta, cur.y, cur.z);
            await Move(left);
        }

        private async void OnTriggerEnter(Collider other)
        {
            if (!other.gameObject.CompareTag("pvx"))
            {
                return;
            }
            if (!master || !_board.Remove(sequence))
            {
                return;
            }
            await Messenger.SendAsync(MessageType.Destroy, sequence, true);
            await Messenger.SendAsync(MessageType.Collision, sequence, false);
        }
        
        public override void Setup(int oid, bool owner)
        {
            base.Setup(oid,owner);
            Messenger.RegisterMessageHandler(MessageType.Move,sequence, (sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    _end = buffer.GetVector3();
                    _sent = false;
                });
            });
            Messenger.RegisterMessageHandler(MessageType.OnAction,sequence, (sessionId, data) =>
            {
                //Debug.Log("on action");
            });
            Messenger.RegisterMessageHandler(MessageType.Destroy,sequence, (sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    DestroyImmediate(gameObject);    
                });
            });
        }
    }
}