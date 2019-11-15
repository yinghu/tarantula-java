using System.Collections;
using System.Collections.Generic;
namespace GameEngineCluster.Model{
    public class Header{
        public string name { get; set; }
        public string value { get; set; }
        public Header(string name,string value){
            this.name = name;
            this.value = value;
        }
    }
    public class Payload{
        public string command;
        public Header[] headers;
    }
    public class Streaming{
        public string action { get; set; }
        public string path { get; set; }
        public bool streaming { get; set; }
        public string label { get; set; }
        public string applicationId { get; set; }
        public string instanceId { get; set; }
        public string tag { get; set; }
        public Payload data{get;set;}
    }
    public class Device{
        public string deviceId { get; set; }
    }
    public class User{
        public string login { get; set; }
        public string nickname { get; set; }
        public string emailAddress { get; set; }
        public string password { get; set; }
    }
    public class Presence{
        public bool successful { get; set; }
        public string systemId { get; set; }
        public int stub { get; set; }
        public string token { get; set; }
        public string ticket { get; set; }
        public string balance { get; set; }
        public string login { get; set; }
    }
    public class Connection{
        public string command { get; set; }
        public int code { get; set; }
        public int timestamp { get; set; }
        public int sequence { get; set; }
        public bool successful { get; set; }
        public string path { get; set; }
        public string protocol { get; set; }
        public string host { get; set; }
        public string type { get; set; }
        public string serverId { get; set; }
        public bool secured { get; set; }
        public int port { get; set; }
    }
    public class Descriptor1{
        public bool singleton { get; set; }
        public int deployCode { get; set; }
        public string type { get; set; }
        public string typeId { get; set; }
        public string category { get; set; }
        public int capacity { get; set; }
        public string name { get; set; }
        public string description { get; set; }
        public string applicationId { get; set; }
        public int accessMode { get; set; }
        public double entryCost { get; set; }
        public string entryCostAsString { get; set; }
        public bool tournamentEnabled { get; set; }
        public bool disabled { get; set; }
        public bool resetEnabled { get; set; }
        public int timerOnModule { get; set; }
        public int runtimeDuration { get; set; }
        public int runtimeDurationOnInstance { get; set; }
        public string tag { get; set; }
        public string icon { get; set; }
        public string viewId { get; set; }
        public string responseLabel { get; set; }
    }
    public class Profile{
        public string nickname { get; set; }
        public string avatar { get; set; }
    }
    public class ArenaZone{
        public string name { get; set; }
        public int rank { get; set; }
        public string description { get; set; }
        public bool enabled { get; set; }
        public List<Arena> list { get; set; }
    }
    public class Arena{
        public string name { get; set; }
        public int level { get; set; }
        public string description { get; set; }
        public bool enabled { get; set; }
    }
}