package lotro.web;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class BypassSSL
{
   public static InputStream openStream(final String address)
   {
      InputStream input = null;
      
      try
      {
         // Create a trust manager that does not validate certificate chains
         final TrustManager[] trustAllCerts = new TrustManager[] { 
                  new X509TrustManager()
                  {
                     @Override
                     public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                     {
                     }
                     
                     @Override
                     public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                     {
                     }
                     
                     @Override
                     public X509Certificate[] getAcceptedIssuers()
                     {
                        return null;
                     }
                  } };
         
         // Install the all-trusting trust manager
         final SSLContext sslContext = SSLContext.getInstance("SSL");
         sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
         // Create an ssl socket factory with our all-trusting manager
         final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
         
         // may need this TODO
         // System.setProperty( "sun.security.ssl.allowUnsafeRenegotiation", "true" );

         // All set up, we can get a resource through https now:
         final URLConnection urlCon = new URL(address).openConnection();
         // Tell the url connection object to use our socket factory which bypasses security checks
         if (urlCon instanceof HttpsURLConnection)
            ((HttpsURLConnection) urlCon).setSSLSocketFactory(sslSocketFactory);

         input = urlCon.getInputStream();
      }
      catch (final Exception x)
      {
         x.printStackTrace(System.err);
      }
      
      return input;
   }
}
