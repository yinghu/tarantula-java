using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using Tarantula.Networking;

public class Integration : MonoBehaviour{
    
    public string GEC_HOST;
    
    public GameEngineCluster gec;
    
    
    private static Integration instance;
    private bool onNotification;

	public static Integration Instance{
		get
		{
			if (instance == null)
			{
				instance = FindObjectOfType<Integration>();
				if (instance == null)
				{
					GameObject obj = new GameObject
					{
						name = typeof(Integration).Name
					};
					instance = obj.AddComponent<Integration>();
				}
			}

			return instance;
		}
	}
    
    
    void Awake(){
         gec = new GameEngineCluster(GEC_HOST);
         DontDestroyOnLoad(this.gameObject);
    }
    void Start(){
        onNotification = false;   
    }

   
    void Update()
    {
        
    }
    public async void profile(){
        bool suc = await gec.Profile(this);
        Debug.Log(gec.profile.nickname);
    }
    public async void notification(){
        onNotification = !onNotification;
        bool suc = await gec.OnNotification("perfect-notification",onNotification);
        //Debug.Log(gec.profile.nickname);
    }

}
