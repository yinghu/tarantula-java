using PaperPlaneTools;
using TMPro;
using UnityEngine;
using PerfectDay.GameEngineCluster;
public class LoginScreen : MonoBehaviour
{
	public WelcomeFlowUI WelcomeFlow;
	public TMP_InputField UserNameInput;
	public TMP_InputField PasswordInput;

	public async void OnClickLogin()
	{
		LoadingManager.Instance.Show();
        User u = new User();
        u.login = UserNameInput.text;
        u.password =PasswordInput.text; 
		bool suc = await GecNetworkManager.Instance.Login(u);
        if(suc){
            await GecNetworkManager.Instance.ArenaList();
            WelcomeFlow.StartGame();
        }
        else{
            new Alert("Login Error",GecNetworkManager.Instance.message).SetPositiveButton("OK").Show();
        }
        /**
        var result = await PlayFabManager.Instance.Login(UserNameInput.text, PasswordInput.text);
		if (result.Success)
		{
			WelcomeFlow.StartGame();
		}
		else
		{
			new Alert("Login Error", result.ErrorMessage).SetPositiveButton("OK").Show();
		}**/
		LoadingManager.Instance.Hide();
	}
}