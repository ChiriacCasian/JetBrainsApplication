import org.junit.Test;

import java.io.IOException;

public class LastCommonCommitsFinderTest {
    @Test
    public void LastCommonCommitsFinderTest1(){
        LastCommonCommitsFinder lastCommonCommitsFinder = (new LastCommonCommitsFinderFactoryImpl()).create("ChiriacCasian", "RestApiTemplate", "token") ;
        try {
            System.out.println(lastCommonCommitsFinder.findLastCommonCommits("main", "AltMain"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
