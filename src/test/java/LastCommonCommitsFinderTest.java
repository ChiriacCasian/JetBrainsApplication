import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class LastCommonCommitsFinderTest {
    @Test
    /// https://github.com/ChiriacCasian/RestApiTemplate/network
    public void TestLastCommonCommitsFinderOnRestApiTemplate(){
        try {
        LastCommonCommitsFinder lastCommonCommitsFinder = (new LastCommonCommitsFinderFactoryImpl())
                .create("ChiriacCasian", "RestApiTemplate", "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") ;
            Assert.assertEquals(lastCommonCommitsFinder.findLastCommonCommits("master", "AltMaster"), Collections.singletonList("7e84212aa23a39110f567c7ee1683f19683f0863"));
            Assert.assertEquals(lastCommonCommitsFinder.findLastCommonCommits("master", "new-remote-branch"), Collections.singletonList("7e84212aa23a39110f567c7ee1683f19683f0863"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
