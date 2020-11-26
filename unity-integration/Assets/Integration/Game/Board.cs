using System.Collections.Generic;
using GameClustering;
using TMPro;
using UnityEngine;

namespace Integration.Game
{
    public class Board : ClusteringObject
    {
        public CameraAdapter cameraAdapter;
        public Camera mainCamera;
        public Player[] players;
        private int _seat;
        public TMP_Text bText;
        public GameObject freeMove;
        private Vector3 _lastPosition;
        private List<GameObject> _gameObjects;
        private  void Start()
        {
            StartClusteringObject(buffer =>
            {
                buffer.PutInt(_gameObjects.Count);
                _gameObjects.ForEach(gmo =>
                {
                    buffer.PutInt(gmo.GetComponent<FreeMove>().sequence);
                });
            }, buffer =>
            {
                var sz = buffer.GetInt();
                for (var i = 0; i < sz; i++)
                {
                    var gm = Instantiate(freeMove);
                    gm.GetComponent<FreeMove>().Setup(buffer.GetInt(),false);
                }
            });
            _gameObjects = new List<GameObject>();
            _lastPosition = freeMove.transform.position;
            Messenger.RegisterMessageHandler(MessageType.Spawn,sequence, (sessionId, data) =>
            {
                MessageContext.Instance.Execute(data, buffer =>
                {
                    var oid = buffer.GetInt();
                    var pos = buffer.GetVector3();
                    pos.y = freeMove.transform.position.y;
                    var fm = Instantiate(freeMove,pos,Quaternion.identity);
                    var fmc = fm.GetComponent<FreeMove>();
                    fmc.Setup(oid,sessionId==Manager.SessionId);
                    if (fmc.master)
                    {
                        _gameObjects.Add(fm);
                    }
                });    
            });
            _seat = Manager.Presence.Seat;
            //if (_seat % 2 == 1)
            //{
                //var pos = mainCamera.transform.rotation;
                //mainCamera.transform.Rotate(pos.x, pos.y, pos.z + 180);
            //}
            bText.text = "SessionId->"+Manager.SessionId;
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
            cameraAdapter.Adapt(hit.point);
        }
        public async void OnFreeMove()
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutInt(Messenger.Sequence());
                buffer.PutVector3(_lastPosition);
                await Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer);
            }
        }
    }
}