package jp.openstandia.connector.github.testutil;

import jp.openstandia.connector.github.GitHubEMUSchema;
import jp.openstandia.connector.github.GitHubClient;

public class MockClient implements GitHubClient<GitHubEMUSchema> {

    private static final MockClient INSTANCE = new MockClient();

    public static MockClient instance() {
        return INSTANCE;
    }

    private MockClient() {
    }

    public void init() {
    }

    @Override
    public void setInstanceName(String instanceName) {
    }

    @Override
    public void test() {
    }

    @Override
    public void auth() {

    }

    @Override
    public void close() {

    }
}
