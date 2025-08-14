package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * GHEnterprise class.
 *
 * @author Hiroyuki Wada
 */
public class GHEnterpriseExt extends GHOrganization {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    GHEnterpriseExt wrapUp(GitHub root) {
        return (GHEnterpriseExt) super.wrapUp(root);
    }

    public SCIMEMUUser createSCIMEMUUser(SCIMEMUUser newUser) throws IOException {
        newUser.schemas = new String[]{SCIMConstants.SCIM_USER_SCHEMA};

        String json = mapper.writeValueAsString(newUser);
        byte[] jsonBytes = json.getBytes();

        try (InputStream inputStream = new ByteArrayInputStream(jsonBytes)) {
            SCIMEMUUser u = root.createRequest()
                    .method("POST")
                    .withHeader(SCIMConstants.HEADER_CONTENT_TYPE, SCIMConstants.SCIM_CONTENT_TYPE)
                    .withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT)
                    .withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION)
                    .with(inputStream)
                    .withUrlPath(String.format("/scim/v2/enterprises/%s/Users", login))
                    .fetch(SCIMEMUUser.class);
            return u;
        }
    }

    public SCIMEMUUser updateSCIMEMUUser(String scimUserId, SCIMPatchOperations operations) throws IOException {
        String json = mapper.writeValueAsString(operations);
        byte[] jsonBytes = json.getBytes();

        try (InputStream inputStream = new ByteArrayInputStream(jsonBytes)) {
            SCIMEMUUser u = root.createRequest()
                    .method("PATCH")
                    .withHeader(SCIMConstants.HEADER_CONTENT_TYPE, SCIMConstants.SCIM_CONTENT_TYPE)
                    .withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT)
                    .withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION)
                    .with(inputStream)
                    .withUrlPath(String.format("/scim/v2/enterprises/%s/Users/%s", login, scimUserId))
                    .fetch(SCIMEMUUser.class);
            return u;
        }
    }

    public SCIMEMUUser getSCIMEMUUser(String scimUserId) throws IOException {
        SCIMEMUUser u = root.createRequest()
                .withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT)
                .withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION)
                .withUrlPath(String.format("/scim/v2/enterprises/%s/Users/%s", login, scimUserId))
                .fetch(SCIMEMUUser.class);
        return u;
    }

    public SCIMEMUUser getSCIMEMUUserByUserName(String scimUserName) throws IOException {
        List<SCIMEMUUser> list = searchSCIMUsers()
                .eq("userName", scimUserName)
                .list()
                .toList();
        if (list.size() != 1) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Search users.
     *
     * @return the gh user search builder
     */
    public SCIMEMUUserSearchBuilder searchSCIMUsers() {
        return new SCIMEMUUserSearchBuilder(root, this);
    }

    public SCIMPagedSearchIterable<SCIMEMUUser> listSCIMUsers(int pageSize, int pageOffset)
            throws IOException {
        return searchSCIMUsers().list().withPageSize(pageSize).withPageOffset(pageOffset);
    }

    public void deleteSCIMUser(String scimUserId) throws IOException {
        root.createRequest()
                .method("DELETE")
                .withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT)
                .withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION)
                .withUrlPath(String.format("/scim/v2/enterprises/%s/Users/%s", login, scimUserId))
                .send();
    }

    public SCIMEMUGroup createSCIMEMUGroup(SCIMEMUGroup newGroup) throws IOException {
        newGroup.schemas = new String[]{SCIMConstants.SCIM_GROUP_SCHEMA};

        String json = mapper.writeValueAsString(newGroup);
        byte[] jsonBytes = json.getBytes();

        try (InputStream inputStream = new ByteArrayInputStream(jsonBytes)) {
            SCIMEMUGroup g = root.createRequest()
                    .method("POST")
                    .withHeader(SCIMConstants.HEADER_CONTENT_TYPE, SCIMConstants.SCIM_CONTENT_TYPE)
                    .withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT)
                    .withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION)
                    .with(inputStream)
                    .withUrlPath(String.format("/scim/v2/enterprises/%s/Groups", login))
                    .fetch(SCIMEMUGroup.class);
            return g;
        }
    }

    public SCIMEMUGroup updateSCIMEMUGroup(String scimGroupId, SCIMPatchOperations operations) throws IOException {
        String json = mapper.writeValueAsString(operations);
        byte[] jsonBytes = json.getBytes();

        try (InputStream inputStream = new ByteArrayInputStream(jsonBytes)) {
            SCIMEMUGroup g = root.createRequest()
                    .method("PATCH")
                    .withHeader(SCIMConstants.HEADER_CONTENT_TYPE, SCIMConstants.SCIM_CONTENT_TYPE)
                    .withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT)
                    .withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION)
                    .with(inputStream)
                    .withUrlPath(String.format("/scim/v2/enterprises/%s/Groups/%s", login, scimGroupId))
                    .fetch(SCIMEMUGroup.class);
            return g;
        }
    }

    public SCIMEMUGroup getSCIMEMUGroup(String scimGroupId) throws IOException {
        SCIMEMUGroup g = root.createRequest()
                .withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT)
                .withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION)
                .withUrlPath(String.format("/scim/v2/enterprises/%s/Groups/%s", login, scimGroupId))
                .fetch(SCIMEMUGroup.class);
        return g;
    }

    public SCIMEMUGroup getSCIMEMUGroupByDisplayName(String scimGroupDisplayName) throws IOException {
        List<SCIMEMUGroup> list = searchSCIMGroups()
                .eq("displayName", scimGroupDisplayName)
                .list()
                .toList();
        if (list.size() != 1) {
            return null;
        }
        return list.get(0);
    }

    public GitHubCopilotSeat getCopilotSeatByDisplayName(String copilotSeatDisplayName) throws IOException {
        List<GitHubCopilotSeat> allSeats = searchCopilotSeats()
                .list()
                .toList();

        return allSeats.stream()
                .filter(seat -> copilotSeatDisplayName.equals(seat.assignee.login))
                .findFirst()
                .orElse(null);
    }

    public GitHubCopilotSeat getCopilotSeatByUid(String copilotSeatUid) throws IOException {
        List<GitHubCopilotSeat> allSeats = searchCopilotSeats()
                .list()
                .toList();

        return allSeats.stream()
                .filter(seat -> copilotSeatUid.equals(seat.assignee.id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Search groups.
     *
     * @return the gh group search builder
     */
    public SCIMEMUGroupSearchBuilder searchSCIMGroups() {
        return new SCIMEMUGroupSearchBuilder(root, this);
    }

    public SCIMPagedSearchIterable<SCIMEMUGroup> listSCIMGroups(int pageSize, int pageOffset)
            throws IOException {
        return searchSCIMGroups().list().withPageSize(pageSize).withPageOffset(pageOffset);
    }

    public GitHubCopilotSeatsSearchBuilder searchCopilotSeats() {
        return new GitHubCopilotSeatsSearchBuilder(root, this);
    }

    public GitHubCopilotSeatPagedSearchIterable<GitHubCopilotSeat> listAllSeats(int pageSize, int pageOffset)
            throws IOException {
        return searchCopilotSeats().list().withPageSize(pageSize).withPageOffset(pageOffset);
    }

    public void deleteSCIMGroup(String scimGroupId) throws IOException {
        root.createRequest()
                .method("DELETE")
                .withHeader(SCIMConstants.HEADER_ACCEPT, SCIMConstants.SCIM_ACCEPT)
                .withHeader(SCIMConstants.HEADER_API_VERSION, SCIMConstants.GITHUB_API_VERSION)
                .withUrlPath(String.format("/scim/v2/enterprises/%s/Groups/%s", login, scimGroupId))
                .send();
    }
}