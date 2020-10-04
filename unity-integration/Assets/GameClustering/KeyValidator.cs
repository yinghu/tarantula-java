using UnityEngine.Networking;

namespace GameClustering
{
    public class KeyValidator : CertificateHandler
    {
        protected override bool ValidateCertificate(byte[] certificateData){
            //put key validation here
            //uncomment this block to valid the certificate
            //X509Certificate2 cert = new X509Certificate2(certificateData);
            return true;
        }
    }
}