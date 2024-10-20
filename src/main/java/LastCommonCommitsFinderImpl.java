import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

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
        HttpURLConnection conn = getConnection(url + branchName, authToken);

        Collection<String> commits = new HashSet<>();
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) { // success
            JSONArray jsonArray = readeJsonFromResponse(conn);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject commit = jsonArray.getJSONObject(i);
                String sha = commit.getString("sha");
                commits.add(sha);
            }
        } else {
            System.out.println("GET request failed: " + responseCode);
        }

        return commits;
    }

    public Collection<String> findLastCommonCommits(String branchA, String branchB) throws IOException {
        String url = "https://api.github.com/repos/" + this.owner + "/" + this.repo + "/commits?sha=" ;

        Collection<String> commmitsA = findSha1sFromBranch(url, branchA, this.token) ;
        Collection<String> commmitsB = findSha1sFromBranch(url, branchB, this.token) ;

        Collection<String> commonCommits = new HashSet<>(commmitsA);
        commonCommits.retainAll(commmitsB);

        return findMergeBases(commonCommits, this.token) ;
    }

    public Collection<String> findMergeBases(Collection<String> commonCommits, String authToken) {
        try {
            Collection<String> mergeBases = new ArrayList<>();

            for (String commonCommit : commonCommits) {
                Collection<String> children = getChildrenOfCommit(commonCommit, authToken);
                /// check if the children are NON-common commits, if they are the commonCommit is a mergeBase
                boolean isMergeBase = true;

                for (String child : children) {
                    if (commonCommits.contains(child)) {
                        isMergeBase = false;
                        break;
                    }
                }

                if (isMergeBase) {
                    mergeBases.add(commonCommit);
                }
            }
            return mergeBases;
        }catch (IOException e){
            System.out.println("couldn't get commit properties");
            throw new RuntimeException(e);
        }
    }
    public Collection<String> getChildrenOfCommit(String commitSha, String authToken) throws IOException {
        Collection<String> children = new ArrayList<>();
        String apiUrl = "https://api.github.com/repos/" + this.owner + "/" + this.repo + "/commits?sha=" + commitSha;

        HttpURLConnection conn = getConnection(apiUrl, authToken);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            JSONArray commits = readeJsonFromResponse(conn);
            for (int i = 0; i < commits.length(); i++) {
                JSONObject commit = commits.getJSONObject(i);
                JSONArray parents = commit.getJSONArray("parents");
                for (int j = 0; j < parents.length(); j++) {
                    String parentSha = parents.getJSONObject(j).getString("sha");
                    if (parentSha.equals(commitSha)) {
                        // If the current commit's parent is the commitSha we're interested in, add it as a child
                        String childSha = commit.getString("sha");
                        children.add(childSha);
                    }
                }
            }
        } else {
            System.out.println("GET request failed: " + responseCode);
        }

        return children;
    }
    private JSONArray readeJsonFromResponse(HttpURLConnection conn) throws IOException {
        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONArray(response.toString());
    }
}
