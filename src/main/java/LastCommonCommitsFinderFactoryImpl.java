public class LastCommonCommitsFinderFactoryImpl implements LastCommonCommitsFinderFactory{

    LastCommonCommitsFinderFactoryImpl(){}
    public LastCommonCommitsFinder create(String owner, String repo, String token) {
        return new LastCommonCommitsFinderImpl(owner, repo, token) ;
    }
}
