import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class LastCommonCommitsFinderImpl implements LastCommonCommitsFinder{
    private String owner;
    private String repo;
    private String token;
    LastCommonCommitsFinderImpl(String owner, String repo, String token){
        this.owner = owner ;
        this.repo = repo ;
        this.token = token ;
    }

    /*
    There are two ways of doing this :

    using the compare/branchA...branchB
    which in the response has the most recent base commit
    This is very fast but it does not cove the scenario where there are multiple base commits
    Documentation : https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28#compare-two-commits

    getting all the commits sha1s from the first branch and from the second branch and then comparing them to see
    which are the base commits, this is much more computationally expensive especially for large codebases but is
    the only way to find multiple base commits

     */
    private HttpURLConnection getConnection(String url, String authToken) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + authToken);
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        return conn;
    }

    private Collection<String> findSha1sFromBranch(String url, String branchName, String authToken) throws IOException {
        HttpURLConnection conn = getConnection(url + branchName, authToken) ;

        Collection<String> commits = new HashSet<>();
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder responseBody = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                responseBody.append(inputLine);
            }
            in.close();

            commits = Collections.singleton(responseBody.toString());
        } else {
            System.out.println("GET request failed: " + responseCode);
        }

        return commits;
    }

    public Collection<String> findLastCommonCommits(String branchA, String branchB) throws IOException {
        String url = "" ;

        Collection<String> commmitsA = findSha1sFromBranch(url, branchA, this.token) ;
        Collection<String> commmitsB = findSha1sFromBranch(url, branchB, this.token) ;
    }

}
