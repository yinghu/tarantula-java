using System.Collections;
using GameClustering;
using UnityEngine;

namespace Integration.Game
{
    public class Player : ClusteringObject
    {
        
        private const float Speed = 6f;
        private Vector3 _end;
        public GameObject bullet;
        private float _timer;
        private void Start()
        {
            StartClusteringObject(buffer =>
            {
                buffer.PutVector3(transform.position);    
            },
            buffer =>
            {
                _end = buffer.GetVector3();
            });
            _end = transform.position;
            _timer = 0.5f;
            Messenger.RegisterMessageHandler(MessageType.Relay,sequence,(sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    _end = buffer.GetVector3();
                    _end.y = 1;
                });
            });
            Messenger.RegisterMessageHandler(MessageType.Spawn,sequence, (sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    StartCoroutine(FireBullet());
                });
            });
        }

        public async void Move(Vector3 target)
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutVector3(target);
                await Messenger.SendAsync(MessageType.Relay, sequence, true, buffer);
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
            await Messenger.SendAsync(MessageType.Spawn, sequence, true);
        }
        private IEnumerator FireBullet()
        {
            var shot = Instantiate(bullet,transform.position, Quaternion.identity);
            yield return new WaitForSeconds(1);
            Destroy(shot);
        }
    }
    
}