using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Newtonsoft.Json;
[CreateAssetMenu(fileName = "Abilities", menuName = "Scripts/Abilities", order = 1)]
public class Abilities : ScriptableObject{

    public enum Rarities{Common,Rare,Epic,Legendary}

    [Header("General")]
    public float x;
    public float y;
    public float z;
    public Rarities rarity;
    [Header("UI")]
    [JsonIgnore]
    public Sprite CardImage;
}
