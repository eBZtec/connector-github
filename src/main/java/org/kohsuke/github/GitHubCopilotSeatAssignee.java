package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubCopilotSeatAssignee {
    @JsonProperty("login")
    public String login;

    @JsonProperty("id")
    public String id;

    @JsonProperty("node_id")
    public String node_id;

    @JsonProperty("url")
    public String url;

    @JsonProperty("type")
    public String type;

    @JsonProperty("user_view_type")
    public String user_view_type;

    @JsonProperty("site_admin")
    public String site_admin;
}