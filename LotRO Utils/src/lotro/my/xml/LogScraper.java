package lotro.my.xml;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import utils.Utils;

public abstract class LogScraper
{
   protected static final String LOREBOOK_URL =
      "http://lorebook.lotro.com/wiki/Special:LotroResource";
   
   protected static final SimpleDateFormat HTML_DATE = new SimpleDateFormat ("MM/dd/yyyy h:mm a");
   protected static final SimpleDateFormat USER_DATE = new SimpleDateFormat ("MMM dd yyyy");
   
   // <td class="paginate_back"><< Back</td><td class="paginate_info"> Page: 1 of 187</td>
   private static final Pattern PAGES = Pattern.compile ("> Page: [0-9]+ of ([0-9]+)<");

   // filter values: overview, level%20up, quest, deed, pvmp

   public void scrape (final String filter, final String charID) throws Exception
   {
      String page1 = scrape (filter, charID, 1);
      int pages = 1;
      Matcher m = PAGES.matcher (page1);
      if (m.find())
         pages = Integer.parseInt (m.group (1));
      
      int page = 2;
      while (page <= pages)
      {
         System.out.println ("Scraping " + page + " of " + pages + "..."); // TBD
         scrape (filter, charID, page++);
      }
   }
   
   protected abstract void parse (final String response);
   
   private String scrape (final String filter, final String charID, final int page)
   {
      Utils.sleep (Math.round (Math.random() * 8000)); // be nice
      HttpClient client = new HttpClient();
      client.getHostConfiguration().setHost ("my.lotro.com", 80, "http");

      PostMethod post = new PostMethod ("/wp-includes/widgets/ajax.php");
      List<NameValuePair> args = new ArrayList<NameValuePair>();
      args.add (new NameValuePair ("widget", "CharacterLog"));
      args.add (new NameValuePair ("CharacterLog_filter", filter));
      args.add (new NameValuePair ("char_id", charID));
      args.add (new NameValuePair ("world_id", "5")); // Landroval
      args.add (new NameValuePair ("page", page + ""));
      post.setRequestBody (args.toArray (new NameValuePair [args.size()]));
      
      String response = execute (client, post);
      parse (response);
      return response;
   }

   private static String execute (final HttpClient client, final HttpMethod method)
   {
      String response = null;

      try
      {
         method.setDoAuthentication (true);
         // method.setFollowRedirects (true);

         int statusCode = client.executeMethod (method);
         if (statusCode != HttpStatus.SC_OK)
            System.err.println ("Method failed: " + method.getStatusLine());

         // Read the response body.  Use caution: ensure correct character 
         // encoding and is not binary data.
         response = method.getResponseBodyAsString();
      }
      catch (HttpException x)
      {
         System.err.println ("Fatal protocol violation: " + x.getMessage());
         x.printStackTrace();
      }
      catch (IOException x)
      {
         System.err.println ("Fatal transport error: " + x.getMessage());
         x.printStackTrace();
      }
      finally
      {
         method.releaseConnection();
      }

      return response;
   }
}
