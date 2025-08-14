package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitHubCopilotSeat {
    @JsonProperty("created_at")
    public String created_at;

    @JsonProperty("assignee")
    public GitHubCopilotSeatAssignee assignee;

    @JsonProperty("pending_cancellation_date")
    public String pending_cancellation_date;

    @JsonProperty("plan_type")
    public String plan_type;

    @JsonProperty("last_authenticated_at")
    public String last_authenticated_at;

    @JsonProperty("updated_at")
    public String updated_at;

    @JsonProperty("last_activity_at")
    public String last_activity_at;

    @JsonProperty("last_activity_editor")
    public String last_activity_editor;

    @JsonProperty("assigning_team")
    public GitHubCopilotSeatAssigningTeam assigning_team;
}