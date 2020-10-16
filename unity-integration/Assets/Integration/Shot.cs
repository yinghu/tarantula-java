using GameClustering;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace Integration
{
    public class Shot : MonoBehaviour
    {
        public async void Roll()
        {
            var integrationManager = IntegrationManager.Instance;
            var buffer1 = new DataBuffer();
            buffer1.PutInt(1);
            await integrationManager.Messenger.SendAsync(MessageType.Echo, 1, false, buffer1);
            var buffer2 = new DataBuffer();
            buffer2.PutInt(2);
            await integrationManager.Messenger.SendAsync(MessageType.Echo, 2, false, buffer2);
            Debug.Log("Shooting ...");
        }

        public void Exit()
        {
            SceneManager.LoadScene("Main");
        }
    }
}