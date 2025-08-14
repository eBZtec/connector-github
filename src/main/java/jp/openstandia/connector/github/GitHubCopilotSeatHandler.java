package jp.openstandia.connector.github;

import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.kohsuke.github.GitHubCopilotSeat;
import org.kohsuke.github.SCIMPatchOperations;

import java.util.Set;

import static jp.openstandia.connector.util.Utils.toZoneDateTime;
import static jp.openstandia.connector.util.Utils.toZoneDateTimeForISO8601OffsetDateTime;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.*;

public class GitHubCopilotSeatHandler extends AbstractGitHubEMUHandler {

    public static final ObjectClass SEAT_OBJECT_CLASS = new ObjectClass("GitHubCopilotSeat");

    private static final Log LOGGER = Log.getLog(GitHubCopilotSeatHandler.class);

    public GitHubCopilotSeatHandler(GitHubEMUConfiguration configuration, GitHubClient<GitHubEMUSchema> client,
                                    GitHubEMUSchema schema, SchemaDefinition schemaDefinition) {
        super(configuration, client, schema, schemaDefinition);
    }

    public static SchemaDefinition.Builder createSchema(AbstractGitHubConfiguration configuration, GitHubClient<GitHubEMUSchema> client) {
        SchemaDefinition.Builder<GitHubCopilotSeat, SCIMPatchOperations, GitHubCopilotSeat> sb
                = SchemaDefinition.newBuilder(SEAT_OBJECT_CLASS, GitHubCopilotSeat.class, SCIMPatchOperations.class, GitHubCopilotSeat.class);

        // __UID__
        // The id for the seat. Must be unique and unchangeable.
        sb.addUid("id",
                SchemaDefinition.Types.UUID,
                null,
                (source) -> source.assignee.id,
                "id",
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        // code (__NAME__)
        // The name for the seat. Must be unique and changeable.
        sb.addName("displayName",
                SchemaDefinition.Types.STRING_CASE_IGNORE,
                (source, dest) -> dest.assignee.login = source,
                (source, dest) -> dest.replace("displayName", source),
                (source) -> source.assignee.login,
                null,
                REQUIRED
        );

        // Metadata (readonly)
        sb.add("created_at",
                SchemaDefinition.Types.DATETIME,
                null,
                (source) -> source.created_at != null ? toZoneDateTimeForISO8601OffsetDateTime(source.created_at) : null,
                null,
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        sb.add("last_authenticated_at",
                SchemaDefinition.Types.DATETIME,
                null,
                (source) -> source.last_authenticated_at != null ? toZoneDateTimeForISO8601OffsetDateTime(source.last_authenticated_at) : null,
                null,
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        sb.add("updated_at",
                SchemaDefinition.Types.DATETIME,
                null,
                (source) -> source.updated_at != null ? toZoneDateTimeForISO8601OffsetDateTime(source.updated_at) : null,
                null,
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        sb.add("last_activity_at",
                SchemaDefinition.Types.DATETIME,
                null,
                (source) -> source.last_activity_at != null ? toZoneDateTimeForISO8601OffsetDateTime(source.last_activity_at) : null,
                null,
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        sb.add("pending_cancellation_date",
                SchemaDefinition.Types.DATETIME,
                null,
                (source) -> source.pending_cancellation_date != null ? toZoneDateTime(source.pending_cancellation_date) : null,
                null,
                NOT_CREATABLE, NOT_UPDATEABLE
        );

        sb.add("last_activity_editor",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    dest.last_activity_editor = source;
                },
                (source, dest) -> dest.replace("last_activity_editor", source),
                (source) -> source.last_activity_editor,
                null
        );

        sb.add("plan_type",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    dest.plan_type = source;
                },
                (source, dest) -> dest.replace("plan_type", source),
                (source) -> source.plan_type,
                null
        );

        sb.add("assignee.type",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    dest.assignee.type = source;
                },
                (source, dest) -> dest.replace("assignee.type", source),
                (source) -> source.assignee.type,
                null
        );

        sb.add("assigning_team.slug",
                SchemaDefinition.Types.STRING,
                (source, dest) -> {
                    if (source != null) {
                        dest.assigning_team.slug = source;
                    }
                },
                (source, dest) -> dest.replace("assigning_team.slug", source),
                (source) -> source != null && source.assigning_team != null ? source.assigning_team.slug : null,
                null
        );

        LOGGER.ok("The constructed GitHub EMU Seat schema");

        return sb;
    }

    @Override
    public Uid create(Set<Attribute> attributes) {
        return null;
    }

    @Override
    public Set<AttributeDelta> updateDelta(Uid uid, Set<AttributeDelta> modifications, OperationOptions options) {
        return Set.of();
    }

    @Override
    public void delete(Uid uid, OperationOptions options) {
    }

    @Override
    public int getByUid(Uid uid, ResultsHandler resultsHandler, OperationOptions options, Set<String> returnAttributesSet, Set<String> fetchFieldsSet, boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
        GitHubCopilotSeat seat = client.getCopilotSeat(uid, options, fetchFieldsSet);

        if (seat != null) {
            resultsHandler.handle(toConnectorObject(schemaDefinition, seat, returnAttributesSet, allowPartialAttributeValues));
            return 1;
        }
        return 0;
    }

    @Override
    public int getByName(Name name, ResultsHandler resultsHandler, OperationOptions options, Set<String> returnAttributesSet, Set<String> fetchFieldsSet, boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
        GitHubCopilotSeat seat = client.getCopilotSeat(name, options, fetchFieldsSet);

        if (seat != null) {
            resultsHandler.handle(toConnectorObject(schemaDefinition, seat, returnAttributesSet, allowPartialAttributeValues));
            return 1;
        }
        return 0;
    }

    @Override
    public int getByMembers(Attribute attribute, ResultsHandler resultsHandler, OperationOptions options, Set<String> returnAttributesSet, Set<String> fetchFieldSet, boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
        return super.getByMembers(attribute, resultsHandler, options, returnAttributesSet, fetchFieldSet, allowPartialAttributeValues, pageSize, pageOffset);
    }

    @Override
    public int getAll(ResultsHandler resultsHandler, OperationOptions options, Set<String> returnAttributesSet, Set<String> fetchFieldsSet, boolean allowPartialAttributeValues, int pageSize, int pageOffset) {
        return client.getCopilotSeats((s) -> resultsHandler.handle(toConnectorObject(schemaDefinition, s, returnAttributesSet, allowPartialAttributeValues)),
                options, fetchFieldsSet, pageSize, pageOffset);
    }

    @Override
    public void query(GitHubFilter filter, ResultsHandler resultsHandler, OperationOptions options) {
        super.query(filter, resultsHandler, options);
    }

    @Override
    public <T> ConnectorObject toConnectorObject(SchemaDefinition schema, T user, Set<String> returnAttributesSet, boolean allowPartialAttributeValues) {
        return super.toConnectorObject(schema, user, returnAttributesSet, allowPartialAttributeValues);
    }
}