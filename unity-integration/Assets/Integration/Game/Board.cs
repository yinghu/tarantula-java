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
        private Player[] _players;
        private int _seat;
        public TMP_Text bText;
        public GameObject freeMove;
        public GameObject[] players;
        private Vector3 _lastPosition;
        private List<GameObject> _gameObjects;
        private async void Start()
        {
         
            StartClusteringObject(buffer =>
            {
                buffer.PutInt(_gameObjects.Count);
                _gameObjects.ForEach(gmo =>
                {
                    var fm = gmo.GetComponent<ClusteringObject>();
                    buffer.PutInt(fm.typeId);
                    buffer.PutInt(fm.sequence);
                });
            }, buffer =>
            {
                var sz = buffer.GetInt();
                for (var i = 0; i < sz; i++)
                {
                    var gm = Instantiate(freeMove);
                    gm.GetComponent<ClusteringObject>().Setup(buffer.GetInt(),buffer.GetInt(),false);
                }
            });
            _gameObjects = new List<GameObject>();
            _lastPosition = freeMove.transform.position;
            Messenger.RegisterMessageHandler(MessageType.Spawn,sequence, (sessionId, data) =>
            {
                MessageContext.Instance.Execute(data, buffer =>
                {
                    var opt = buffer.GetInt();
                    switch (opt)
                    {
                        case 1:
                            var oid = buffer.GetInt();
                            var pos = buffer.GetVector3();
                            pos.y = freeMove.transform.position.y;
                            var fm = Instantiate(freeMove, pos, Quaternion.identity);
                            var fmc = fm.GetComponent<ClusteringObject>();
                            fmc.Setup(1,oid, sessionId == Manager.SessionId);
                            if (fmc.master)
                            {
                                _gameObjects.Add(fm);
                            }
                            break;
                        case 2:
                            //spawn player
                            var pid = buffer.GetInt();
                            var player = Instantiate(players[Manager.Presence.Seat]).GetComponent<Player>();
                            player.Setup(2,pid,sessionId==Manager.SessionId);
                            _players[Manager.Presence.Seat] = player;
                            break;
                    }
                });    
            });
            _seat = Manager.Presence.Seat;
            _players = new Player[Manager.Presence.Capacity];
            using (var buffer = new DataBuffer())
            {
                buffer.PutInt(2);
                buffer.PutInt(Messenger.Sequence());
                //buffer.PutVector3(_lastPosition);
                await Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer);
            }
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
            _players[_seat].Move(hit.point);
            cameraAdapter.Adapt(hit.point);
        }
        public async void OnFreeMove()
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutInt(1);
                buffer.PutInt(Messenger.Sequence());
                buffer.PutVector3(_lastPosition);
                await Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer);
            }
        }
    }
}