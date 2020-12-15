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
        public Transform firePoint;
        private float _timer;
        public bool GameStart { set; get; }

        private void Start()
        {
            StartClusteringObject(async buffer =>
            {
                buffer.PutVector3(transform.position);
                await Messenger.SendAsync(MessageType.OnSync, sequence, true, buffer);
            },
            buffer =>
            {
                _end = buffer.GetVector3();
            });
            _end = new Vector3(0,1,0);
            _timer = 0.5f;
           
            Messenger.RegisterMessageHandler(MessageType.Move,sequence,(sessionId, data) =>
            {
                MainThread.Execute(data, buffer =>
                {
                    _end = buffer.GetVector3();
                    _end.y = 1;
                });
            });
            Messenger.RegisterMessageHandler(MessageType.OnAction,sequence, (sessionId, data) =>
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
                await Messenger.SendAsync(MessageType.Move, sequence, true, buffer);
            }
        }

        private  void FixedUpdate()
        {
            if (master&&!GameStart)
            {
                return;
            }

            transform.position = Vector3.Lerp(transform.position, _end, Speed*Time.fixedDeltaTime);
            _timer -= Time.fixedDeltaTime;
            transform.LookAt(_end);
            if (_timer > 0)
            {
                return;
            }
            _timer = 0.5f;
            //await Messenger.SendAsync(MessageType.Action, sequence, true);
        }
        private IEnumerator FireBullet()
        {
            var shot = Instantiate(bullet);
            var ray = new Ray(firePoint.position,transform.forward);
            shot.GetComponent<Bullet>().Fire(ray.origin,ray.GetPoint(10f));
            yield return new WaitForSeconds(1);
            Destroy(shot);
        }
    }
    
}