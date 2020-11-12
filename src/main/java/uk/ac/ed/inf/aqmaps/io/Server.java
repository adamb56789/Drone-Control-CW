package uk.ac.ed.inf.aqmaps.io;

/** Handles requesting data from a server. */
public interface Server {
  /**
   * Request the data that is located at the given URL. Will cause a fatal error if it cannot
   * connect to the server, or if the requested file is not found.
   *
   * @param url the URL of the file to request
   * @return the requested data as a String
   */
  String requestData(String url);
}
