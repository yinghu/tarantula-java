using UnityEngine;

namespace Holee
{
    public class Ping : MonoBehaviour
    {
        private float _timer;
        private bool _joined;
        private byte[] _ping;
        private void Start()
        {
            Debug.Log("Start Ping");
        }
        
        private void FixedUpdate()
        {
            if(!_joined) return;
            _timer += Time.deltaTime;//20ms per frame
            if (_timer < 1) return;
            NetworkingManager.Send(_ping,_ping.Length);
            _timer = 0;
        }

        public void OnJoin(byte[] ping)
        {
            _ping = ping;
            _joined = true;
        }

    }
}