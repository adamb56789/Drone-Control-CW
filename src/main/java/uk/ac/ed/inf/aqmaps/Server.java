package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Instances of this class are used to make requests from the web server at the specified URL. This
 * class is implemented separately so that it can be easily mocked for testing.
 */
public class Server {
  private final HttpClient client = HttpClient.newHttpClient();

  /**
   * Request the data that is located at the given URL. Will cause a fatal error if it cannot
   * connect to the server, or if the requested file is not found.
   *
   * @param url the URL of the file to request
   * @return the requested data as a String
   */
  public String requestData(String url) {
    String returnValue = "";
    try {
      var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        // 200 means OK
        returnValue = response.body();
      } else {
        // any other status code means the data has not been acquired successfully
        System.out.println("Fatal error: " + url + " returned status " + response.statusCode());
        System.exit(1);
      }

    } catch (IOException | InterruptedException e) {
      // This is normally a java.net.ConnectException if there is no server running on the port
      System.out.println("Fatal error: Unable to connect to " + url);
      System.exit(1);
    }
    return returnValue; // This should never be reached, but the compiler isn't figuring that out.
  }
}
