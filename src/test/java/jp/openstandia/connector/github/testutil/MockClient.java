package jp.openstandia.connector.github.testutil;

import jp.openstandia.connector.github.GitHubEMUSchema;
import jp.openstandia.connector.github.GitHubClient;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.kohsuke.github.SCIMUser;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MockClient implements GitHubClient<GitHubEMUSchema> {

    private static final MockClient INSTANCE = new MockClient();

    public static MockClient instance() {
        return INSTANCE;
    }

    private MockClient() {
    }

    public void init() {
    }

    public void setInstanceName(String instanceName) {
    }

    public void test() {

    }

    public void auth() {

    }

    public Uid createUser(GitHubEMUSchema schema, SCIMUser scimUser) throws AlreadyExistsException {
        return null;
    }

    public String updateUser(GitHubEMUSchema schema, Uid uid, String scimUserName, String scimEmail, String scimGivenName, String scimFamilyName, String login, OperationOptions options) throws UnknownUidException {
        return null;
    }

    public void deleteUser(GitHubEMUSchema schema, Uid uid, OperationOptions options) throws UnknownUidException {

    }

    public void getUsers(GitHubEMUSchema schema, ResultsHandler handler, OperationOptions options, Set<String> attributesToGet, boolean allowPartialAttributeValues, int queryPageSize) {

    }

    public void getUser(GitHubEMUSchema schema, Uid uid, ResultsHandler handler, OperationOptions options, Set<String> attributesToGet, boolean allowPartialAttributeValues, int queryPageSize) {

    }

    public void getUser(GitHubEMUSchema schema, Name name, ResultsHandler handler, OperationOptions options, Set<String> attributesToGet, boolean allowPartialAttributeValues, int queryPageSize) {

    }

    public List<String> getTeamIdsByUsername(String userLogin, int pageSize) {
        return null;
    }

    public boolean isOrganizationMember(String userLogin) {
        return false;
    }

    public void assignOrganizationRole(String userLogin, String organizationRole) {

    }

    public void assignTeams(String login, String role, Collection<String> teams) {

    }

    public void unassignTeams(String login, Collection<String> teams) {

    }

    public Uid createTeam(GitHubEMUSchema schema, String teamName, String description, String privacy, Long parentTeamDatabaseId) throws AlreadyExistsException {
        return null;
    }

    public Uid updateTeam(GitHubEMUSchema schema, Uid uid, String teamName, String description, String privacy, Long parentTeamId, boolean clearParent, OperationOptions options) throws UnknownUidException {
        return null;
    }

    public void deleteTeam(GitHubEMUSchema schema, Uid uid, OperationOptions options) throws UnknownUidException {

    }

    public void getTeams(GitHubEMUSchema schema, ResultsHandler handler, OperationOptions options, Set<String> attributesToGet, boolean allowPartialAttributeValues, int queryPageSize) {

    }

    public void getTeam(GitHubEMUSchema schema, Uid uid, ResultsHandler handler, OperationOptions options, Set<String> attributesToGet, boolean allowPartialAttributeValues, int queryPageSize) {

    }

    public void getTeam(GitHubEMUSchema schema, Name name, ResultsHandler handler, OperationOptions options, Set<String> attributesToGet, boolean allowPartialAttributeValues, int queryPageSize) {

    }

    @Override
    public void close() {

    }
}
