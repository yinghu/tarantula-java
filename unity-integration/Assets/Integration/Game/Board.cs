using Integration.Move;
using UnityEngine;

namespace Integration.Game
{
    public class Board : MonoBehaviour
    {
        private Camera _mainCamera;
        public Player player;

        private void Start()
        {
            _mainCamera = Camera.main;
        }

        private void Update()
        {
            if (!Input.GetMouseButtonDown(0))
            {
                return;
            }
            var target = Input.mousePosition;
            if (!Physics.Raycast(_mainCamera.ScreenPointToRay(new Vector3(target.x, target.y)), out var hit))
            {
                return;
            }
            player.Move(hit.point);
        }
    }
}