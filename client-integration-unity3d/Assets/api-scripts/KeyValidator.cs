using UnityEngine.Networking;
using System.Security.Cryptography.X509Certificates;
using UnityEngine;
public class KeyValidator : CertificateHandler
{

    protected override bool ValidateCertificate(byte[] certificateData)
    {
        //put key validation here
        return true;
    }
}

