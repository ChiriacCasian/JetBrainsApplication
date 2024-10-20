import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Implementation of the LastCommonCommitsFinder interface.
 */
public class LastCommonCommitsFinderImpl implements LastCommonCommitsFinder {
    private static final String BASE_URL = "https://api.github.com/repos/";
    private static final String COMMITS_ENDPOINT = "/commits?sha=";
    private static final String AUTH_HEADER = "Authorization";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.github.v3+json";

    private final String owner;
    private final String repo;
    private final String token;

    /**
     * Constructor for LastCommonCommitsFinderImpl.
     *
     * @param owner the owner of the repository
     * @param repo the name of the repository
     * @param token the authentication token
     */
    public LastCommonCommitsFinderImpl(String owner, String repo, String token) {
        this.owner = owner;
        this.repo = repo;
        this.token = token;
    }

    /**
     * Establishes an HTTP connection to the given URL with the provided authentication token.
     *
     * @param url the URL to connect to
     * @return the established HttpURLConnection
     * @throws IOException if an I/O error occurs
     */
    private HttpURLConnection getConnection(String url) throws IOException {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty(AUTH_HEADER, "Bearer " + token);
            conn.setRequestProperty(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
            return conn;
        } catch (IOException e) {
            throw new IOException("Failed to establish connection to URL: " + url, e);
        }
    }

    /**
     * Retrieves a list of SHA-1 hashes from a given branch.
     *
     * @param branchName the name of the branch
     * @return a list of SHA-1 hashes
     * @throws IOException if an I/O error occurs
     */
    private List<String> findSha1sFromBranch(String branchName) throws IOException {
        String url = BASE_URL + owner + "/" + repo + COMMITS_ENDPOINT + branchName + "&per_page=100";
        HttpURLConnection conn = getConnection(url);

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Failed to fetch commits from branch: " + branchName + ", HTTP response code: " + responseCode);
            }

            JSONArray jsonArray = readJsonFromResponse(conn);
            List<String> commits = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject commit = jsonArray.getJSONObject(i);
                String sha = commit.getString("sha");
                commits.add(sha);
            }
            return commits;
        } catch (IOException e) {
            throw new IOException("Error getting commits from branch: " + branchName, e);
        }
    }

    /**
     * Finds the last common commits between two branches.
     *
     * @param branchA the name of the first branch
     * @param branchB the name of the second branch
     * @return a collection of SHA-1 hashes of the last common commits
     * @throws IOException if an I/O error occurs
     */
    public Collection<String> findLastCommonCommits(String branchA, String branchB) throws IOException {
        try {
            List<String> commitsA = findSha1sFromBranch(branchA);
            List<String> commitsB = findSha1sFromBranch(branchB);

            Collection<String> commonCommits = new HashSet<>(commitsA);
            commonCommits.retainAll(commitsB);
            return findMergeBases(commonCommits, commitsA, commitsB);
        } catch (IOException e) {
            throw new IOException("Error finding last common commits between branches: " + branchA + " and " + branchB, e);
        }
    }

    /**
     * Finds the merge bases from the common commits by using the algorithm described in Readme.md.
     *
     * @param commonCommits a collection of common commits
     * @param commitsA a list of commits from the first branch
     * @param commitsB a list of commits from the second branch
     * @return a collection of SHA-1 hashes of the merge bases
     */
    private Collection<String> findMergeBases(Collection<String> commonCommits, List<String> commitsA, List<String> commitsB) {
        Collection<String> mergeBases = new ArrayList<>();

        for (int i = 0; i < commitsA.size() - 1; i++) {
            String commit = commitsA.get(i);
            if (commonCommits.contains(commit)) {
                if (!commonCommits.contains(commitsA.get(i + 1))) {
                    mergeBases.add(commit);
                }
            } else {
                if (commonCommits.contains(commitsA.get(i + 1))) {
                    mergeBases.add(commitsA.get(i + 1));
                }
            }
        }
        return mergeBases;
    }

    /**
     * Reads the JSON response from the given connection.
     *
     * @param conn the HttpURLConnection to read from
     * @return a JSONArray containing the response
     * @throws IOException if an I/O error occurs
     */
    private JSONArray readJsonFromResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return new JSONArray(response.toString());
        } catch (IOException e) {
            throw new IOException("Error reading JSON response from connection", e);
        }
    }

    /**
     * Retrieves the commit message for a given SHA. *FOR DEBUG PURPOSES*
     *
     * @param sha the SHA of the commit
     * @return the commit message
     * @throws IOException if an I/O error occurs
     */
    public String getCommitMessageFromSha(String sha) throws IOException {
        String apiUrl = BASE_URL + owner + "/" + repo + "/commits/" + sha;
        HttpURLConnection conn = getConnection(apiUrl);

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Failed to fetch commit message for SHA: " + sha + ", HTTP response code: " + responseCode);
            }

            String responseBody = readResponseBody(conn);
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getJSONObject("commit").getString("message");
        } catch (IOException e) {
            throw new IOException("Error fetching commit message for SHA: " + sha, e);
        }
    }

    /**
     * Reads the response body from the given connection. *FOR DEBUGGING PURPOSES*
     *
     * @param conn the HttpURLConnection to read from
     * @return the response body as a String
     * @throws IOException if an I/O error occurs
     */
    private String readResponseBody(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        } catch (IOException e) {
            throw new IOException("Error reading response from connection", e);
        }
    }
}