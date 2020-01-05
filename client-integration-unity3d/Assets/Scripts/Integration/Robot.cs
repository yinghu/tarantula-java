using System.Collections;
using System.Collections.Generic;
using UnityEngine;
[CreateAssetMenu(fileName = "Robot", menuName = "Scripts/Robot", order = 1)]
public class Robot : ScriptableObject{
    public int level;
    public string viewId;
    public Abilities[] abilities;
}
