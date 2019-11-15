using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.IO;
using System.Text;
using System;
using UnityEngine;
using UnityEngine.SceneManagement;
using GameEngineCluster;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
public class CRotate : MonoBehaviour {

    public NetworkManager api;
	public float speed = 10.0f;
    private bool _active;

    
    void Start () {
		_active=false;
	}
	
    void Update(){
        if(_active){
            transform.Rotate(10,speed,10);
            //transform.position += Vector3.up * 0.01f;
            //transform.position += Vector3.right * 0.01f;
            float horizontalInput = Input.GetAxis("Horizontal");
            //get the Input from Vertical axis
            float verticalInput = Input.GetAxis("Vertical");
            //update the position
            transform.position = transform.position + new Vector3(horizontalInput * speed * Time.deltaTime, verticalInput * speed * Time.deltaTime, 0);
        }
    }
	
	public  void _Update (bool active) {
        _active = active;
        //string s = await hc.GetJson1(this,"/user/index",new Header[]{new Header("Tarantula-tag","index/lobby")});
        //Debug.Log(s);
        //User u = new User();
        //u.login = "anfgfn";
        //u.nickname="annc";
        //u.password = "password";
        //u.emailAddress = "emailA";
         
        //_Parse(inx);
        /**
        await Task.Run(()=>{
        using(JsonTextReader reader = new JsonTextReader(new StringReader(inx))){
            while (reader.Read()){
                if (reader.Value != null&&reader.TokenType==JsonToken.PropertyName){
                    Debug.Log(reader.Path);
                    Debug.Log("NAME->"+(reader.ValueType)+"<1>"+(reader.TokenType)+"<2>"+(reader.Value));
                    reader.Read();
                    Debug.Log("VALUE->"+(reader.ValueType)+"<1>"+(reader.TokenType)+"<2>"+(reader.Value));
                }
                else{
                    //Debug.Log(reader.Path);
                    //Debug.Log("Token->"+(reader.TokenType));
                }
            }
        }});**/
        /**
        StringBuilder sb = new StringBuilder();
        StringWriter sw = new StringWriter(sb);
        using (JsonWriter writer = new JsonTextWriter(sw))
        {
            writer.WriteStartObject();
            writer.WritePropertyName("CPU");
            writer.WriteValue("Intel");
            writer.WritePropertyName("PSU");
            writer.WriteValue("500W");
            writer.WritePropertyName("Drives");
            writer.WriteStartArray();
            writer.WriteValue("DVD read/writer");
            writer.WriteComment("(broken)");
            writer.WriteValue("500 gigabyte hard drive");
            writer.WriteValue("200 gigabyte hard drive");
            writer.WriteEnd();
            writer.WriteEndObject();
        }
        Debug.Log(sb.ToString());    
        **/
        //transform.Rotate(1,speed,0);
        //transform.position += Vector3.up * 0.01f;
        //transform.position += Vector3.right * 0.01f;
        //float horizontalInput = Input.GetAxis("Horizontal");
        //get the Input from Vertical axis
        //float verticalInput = Input.GetAxis("Vertical");
        //update the position
        //transform.position = transform.position + new Vector3(horizontalInput * speed * Time.deltaTime, verticalInput * speed * Time.deltaTime, 0);
	}
    public void PrintException(Exception ex){
        Debug.Log(ex.Message);
        _active = false;
    }
    
    async Task<string> Pass(){
        await Task.Delay(10000).ConfigureAwait(true);
        Debug.Log("Done!!!!");
        //_active = false;
        return "waiting";
    }
    async void _Parse1(string json){
        await Task.Run(()=>{
            try{
                JObject jo = JObject.Parse(json);
                JArray tk = (JArray)jo.SelectToken("lobbyList");
                for(int i=0;i<tk.Count;i++){
                    Debug.Log(">>"+tk[i].SelectToken("descriptor.typeId"));
                    Debug.Log("app->"+((JArray)tk[i].SelectToken("applications")).Count);
                }
                string cmd = (string)jo.SelectToken("lobbyList[0].descriptor.typeId"); 
                string cmd1 = (string)jo.SelectToken("lobbyList[0].applications[0].tag");
                Debug.Log(cmd);
                Debug.Log(cmd1);
            }catch(Exception ex){
                Debug.Log(ex.Message);    
            }
        });
    }
    async void _Parse(string json){    
        await Task.Run(()=>{
            using(JsonTextReader reader = new JsonTextReader(new StringReader(json))){
                while (reader.Read()){
                    if (reader.Value != null&&reader.TokenType==JsonToken.PropertyName){
                        if(reader.Path.Equals("lobbyList")){
                                    
                        }
                        Debug.Log(reader.Path);
                        Debug.Log("NAME->"+(reader.ValueType)+"<1>"+(reader.TokenType)+"<2>"+(reader.Value));
                        reader.Read();
                        Debug.Log("VALUE->"+(reader.ValueType)+"<1>"+(reader.TokenType)+"<2>"+(reader.Value));
                    }
                    else{
                        //Debug.Log(reader.Path);
                        //Debug.Log("Token->"+(reader.TokenType));
                    }
                }
            }
        });    
    }
   
}
