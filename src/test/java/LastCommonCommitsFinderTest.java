import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

public class LastCommonCommitsFinderTest {
    private String getPatToken() throws IOException { /// PAT token is a secret and needs to be kept in a .ignore to commit changes
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);
        }
        return properties.getProperty("PAT_TOKEN");
    }

    @Test
    /// https://github.com/ChiriacCasian/RestApiTemplate/network
    public void TestLastCommonCommitsFinderOnRestApiTemplate(){
        try {
            String patToken = getPatToken();
        LastCommonCommitsFinder lastCommonCommitsFinder = (new LastCommonCommitsFinderFactoryImpl())
                .create("ChiriacCasian", "RestApiTemplate", patToken) ;
            Assert.assertEquals(lastCommonCommitsFinder.findLastCommonCommits("master", "AltMaster"), Collections.singletonList("7e84212aa23a39110f567c7ee1683f19683f0863"));
            Assert.assertEquals(lastCommonCommitsFinder.findLastCommonCommits("master", "new-remote-branch"), Collections.singletonList("7e84212aa23a39110f567c7ee1683f19683f0863"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
