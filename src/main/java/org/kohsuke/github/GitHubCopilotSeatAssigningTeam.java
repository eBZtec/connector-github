package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubCopilotSeatAssigningTeam {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("slug")
    public String slug;

    @JsonProperty("group_name")
    public String group_name;

    @JsonProperty("created_at")
    public String created_at;

    @JsonProperty("updated_at")
    public String updated_at;
}
