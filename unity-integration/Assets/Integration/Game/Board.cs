using System.Collections.Generic;
using System.Threading.Tasks;
using GameClustering;
using TMPro;
using UnityEngine;

namespace Integration.Game
{
    public class Board : ClusteringObject
    {
        public CameraAdapter cameraAdapter;
        public Camera mainCamera;
        public Exit exit;
        private Player[] _players;
        private int _seat;
        public TMP_Text bText;
        public GameObject[] types;
        private Vector3 _lastPosition;
        private const int FreeMoveTypeId = 4;
        private Dictionary<int,GameObject> _gameObjects;

        private int _outbound;
        private int _inbound;
        private int _timer;
        private bool _started;

        private async void Start()
        {
            _gameObjects = new Dictionary<int, GameObject>();
            _lastPosition = types[FreeMoveTypeId].transform.position;
            Messenger.RegisterMessageHandler(MessageType.OnLoad, sequence, (sessionId, data) =>
            {
                if (sessionId == Manager.SessionId)
                {
                    return;
                }
                MainThread.Execute(data, buffer => { 
                    var tid = buffer.GetInt();
                    var oid = buffer.GetInt();
                    var gm = Instantiate(types[tid], buffer.GetVector3(), Quaternion.identity);
                    gm.GetComponent<ClusteringObject>().Setup(oid, false);
                });
            });
            Messenger.RegisterMessageHandler(MessageType.Load,sequence,  (sessionId, data) =>
            {
                if (sessionId != Manager.SessionId)
                {
                    foreach (var gmo in _gameObjects.Values)
                    {
                        MainThread.Execute( async buffer =>
                        {
                            var fm = gmo.GetComponent<ClusteringObject>();
                            buffer.PutInt(fm.typeId);
                            buffer.PutInt(fm.sequence);
                            buffer.PutVector3(fm.transform.position);
                            await Messenger.SendAsync(MessageType.OnLoad, sequence, true, buffer);
                        });
                    }
                    return;
                }
                MainThread.Execute(data, buffer =>
                {
                    _started = buffer.GetByte() == 1;
                    _players[_seat].GameStart = _started;
                });
            });
            Messenger.RegisterMessageHandler(MessageType.Spawn, sequence, (sessionId, data) =>
            {
                MessageContext.Instance.Execute(data, buffer =>
                {
                    var tid = buffer.GetInt();
                    switch (tid)
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            //spawn player
                            var pid = buffer.GetInt();
                            var pm = Instantiate(types[tid]);
                            var player = pm.GetComponent<Player>();
                            player.Setup(pid, sessionId == Manager.SessionId);
                            if (player.master)
                            {
                                _players[tid] = player;
                                _gameObjects[pid] = pm;
                            }

                            break;
                        case FreeMoveTypeId:
                            var oid = buffer.GetInt();
                            var pos = buffer.GetVector3();
                            var freeMove = types[tid];
                            pos.y = freeMove.transform.position.y;
                            var fm = Instantiate(freeMove, pos, Quaternion.identity);
                            var fmc = fm.GetComponent<ClusteringObject>();
                            fmc.Setup(oid, sessionId == Manager.SessionId);
                            if (fmc.master)
                            {
                                _gameObjects[oid] = fm;
                            }

                            break;
                    }
                });
            });
            _seat = Manager.Room.Seat;
            _players = new Player[Manager.Room.Capacity];
            using (var buffer = new DataBuffer())
            {
                buffer.PutInt(_seat);
                buffer.PutInt(Messenger.Sequence());
                await Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer);
            }

            //if (_seat % 2 == 1)
            //{
            //var pos = mainCamera.transform.rotation;
            //mainCamera.transform.Rotate(pos.x, pos.y, pos.z + 180);
            //}
            bText.text = "SessionId->" + Manager.SessionId;
            _outbound = Messenger.TotalOutbound();
            _inbound = Messenger.TotalInbound();
            _timer = 1;
            Manager.OnGameStartEvent += OnGameStart;
            Manager.OnGameClosingEvent += OnGameClosing;
            Manager.OnGameCloseEvent += OnGameEnd;
            Manager.OnGameJoinTimeout += OnGameEnd;
            
            await Messenger.SendAsync(MessageType.Load, sequence, true);
            InvokeRepeating(nameof(Print), 1.0f, 1.0f);
            Manager.OnJoinedEvent += OnJoin;
        }

        private async void Update()
        {
            if (!_started)
            {
                return;
            }

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
            await OnFreeMove();
        }

        private void Print()
        {
            _timer++;
            var rate1 = Messenger.TotalOutbound() - _outbound;
            _outbound = Messenger.TotalOutbound();
            var rate2 = Messenger.TotalInbound() - _inbound;
            _inbound = Messenger.TotalInbound();
            bText.text = "Retries->" + Messenger.TotalRetries() +
                         "\nPending->" + Messenger.PendingMessages() +
                         "\nOutbound->" + Messenger.TotalOutbound() +
                         "\nInbound->" + Messenger.TotalInbound() +
                         "\nTotal Bytes->" + Messenger.TotalBytes() +
                         "\nOutbound Rate->" + rate1 +
                         "\nInbound Rate->" + rate2 +
                         "\nTimer->" + _timer +
                         "\nRank->" + Manager.Presence.Rank +
                         "\nLevel->" + Manager.Presence.Level +
                         "\nXP->" + Manager.Presence.Xp +
                         "\nConnectionId->" + Messenger.Connection().ConnectionId;
        }

        private async Task OnFreeMove()
        {
            using (var buffer = new DataBuffer())
            {
                buffer.PutInt(FreeMoveTypeId);
                buffer.PutInt(Messenger.Sequence());
                buffer.PutVector3(_lastPosition);
                await Messenger.SendAsync(MessageType.Spawn, sequence, true, buffer);
            }
        }

        public bool Remove(int oid)
        {
            return _gameObjects.Remove(oid);
        }

        private void OnGameStart()
        {
            Debug.Log("game start");
            _started = true;
            _players[_seat].GameStart = true;
        }
        private void OnGameClosing()
        {
            Debug.Log("game closing");
            
        }
        private void OnGameEnd()
        {
            Manager.OnJoinedEvent -= OnJoin;
            Debug.Log("game end");
            Messenger.Disconnect();
            exit.ExitOnEnd();
        }
        private void OnDestroy()
        {
            Manager.OnGameStartEvent -= OnGameStart;
            Manager.OnGameClosingEvent -= OnGameClosing;
            Manager.OnGameCloseEvent -= OnGameEnd;
            Manager.OnGameJoinTimeout -= OnGameEnd;
        }
        private void OnJoin(int sessionId)
        {
            Debug.Log("Session joined->"+sessionId);
        }
    }
}