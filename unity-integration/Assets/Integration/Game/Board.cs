using GameClustering;
using TMPro;
using UnityEngine;

namespace Integration.Game
{
    public class Board : MonoBehaviour
    {
        public int sequence;
        public Camera mainCamera;
        public Player[] players;
        private int _seat;
        public TMP_Text bText;
        public GameObject freeMove;
        private IntegrationManager _integrationManager;
        private Vector3 _lastPosition;
        private void Start()
        {
            _lastPosition = freeMove.transform.position;
            _integrationManager = IntegrationManager.Instance;
            _integrationManager.Messenger.RegisterMessageHandler(MessageType.Spawn,sequence, (sessionId, data) =>
                {
                    MessageContext.Instance.Execute(data, buffer =>
                    {
                        var oid = buffer.GetInt();
                        var pos = buffer.GetVector3();
                        pos.y = freeMove.transform.position.y;
                        var fm = Instantiate(freeMove,pos,Quaternion.identity);
                        fm.GetComponent<FreeMove>().Setup(oid,sessionId==_integrationManager.SessionId);
                    });    
                });
            _seat = _integrationManager.Presence.Seat;
            //if (_seat % 2 == 1)
            //{
                //var pos = mainCamera.transform.rotation;
                //mainCamera.transform.Rotate(pos.x, pos.y, pos.z + 180);
            //}
            bText.text = ">>"+_integrationManager.SessionId;
        }

        private void Update()
        {
            if (!Input.GetMouseButtonDown(0))
            {
                return;
            }
            var target = Input.mousePosition;
            if (!Physics.Raycast(mainCamera.ScreenPointToRay(new Vector3(target.x, target.y)), out var hit))
            {
                return;
            }
            _lastPosition = hit.point;
            players[_seat].Move(hit.point);
        }

        public async void OnFreeMove()
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutInt(_integrationManager.Messenger.Sequence());
                buffer.PutVector3(_lastPosition);
                await _integrationManager.Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer);
            }
        }
    }
}