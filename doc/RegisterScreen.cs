using PaperPlaneTools;
using TMPro;
using UnityEngine;
using PerfectDay.GameEngineCluster;
public class RegisterScreen : MonoBehaviour
{
	public WelcomeFlowUI WelcomeFlow;
	
	public TMP_InputField UsernameInput;
	public TMP_InputField DisplayNameInput;
	public TMP_InputField EmailInput;
	public TMP_InputField PasswordInput;
	public TMP_InputField VerifyPasswordInput;
	
	public async void OnClickSignUp()
	{
		if (PasswordInput.text != VerifyPasswordInput.text)
		{
			OnRegisterError("Passwords do not match");
			return;
		}

		LoadingManager.Instance.Show();
        User u = new User();
        u.login = UsernameInput.text;
        u.nickname = DisplayNameInput.text;
        u.password =PasswordInput.text; 
		bool suc = await GecNetworkManager.Instance.Register(u);
        if(!suc){
			OnRegisterError(GecNetworkManager.Instance.message);
		}
		else
		{
			Debug.Log($"Registration Success: {UsernameInput.text}");
			new Alert("Registration Success!").SetPositiveButton("OK").SetOnDismiss(() => { WelcomeFlow.ShowLoginScreen(); }).Show();
		}
        /**
		var result = await PlayFabManager.Instance.RegisterUser(UsernameInput.text, DisplayNameInput.text, EmailInput.text, PasswordInput.text);
		if (!result.Success)
		{
			OnRegisterError(result.ErrorMessage);
		}
		else
		{
			Debug.Log($"Registration Success: {UsernameInput.text}");
			new Alert("Registration Success!").SetPositiveButton("OK").SetOnDismiss(() => { WelcomeFlow.ShowLoginScreen(); }).Show();
		}
        **/
		LoadingManager.Instance.Hide();
	}

	private static void OnRegisterError(string errorMessage)
	{
		Debug.LogWarning($"Registration Error: {errorMessage}");
		new Alert("Registration Error", errorMessage).SetPositiveButton("OK").Show();
	}
}