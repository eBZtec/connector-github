package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.openstandia.connector.github.GitHubCopilotSeatHandler;
import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GitHubCopilotSeatTest {

    @Test
    void testFieldAssignments() {
        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        seat.created_at = "2025-01-01";
        seat.pending_cancellation_date = "2025-02-02";
        seat.plan_type = "pro";
        seat.last_authenticated_at = "2025-01-10";
        seat.updated_at = "2025-02-15";
        seat.last_activity_at = "2025-02-20";
        seat.last_activity_editor = "VSCode";

        GitHubCopilotSeatAssignee assignee = new GitHubCopilotSeatAssignee();
        assignee.login = "dev1";
        seat.assignee = assignee;

        GitHubCopilotSeatAssigningTeam team = new GitHubCopilotSeatAssigningTeam();
        team.id = "t1";
        team.name = "Team1";
        seat.assigning_team = team;

        assertEquals("dev1", seat.assignee.login);
        assertEquals("Team1", seat.assigning_team.name);
    }

    @Test
    void testJsonSerializationDeserialization() throws Exception {
        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        seat.created_at = "2025-01-01";
        seat.plan_type = "enterprise";
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(seat);
        assertTrue(json.contains("2025-01-01"));
        GitHubCopilotSeat restored = mapper.readValue(json, GitHubCopilotSeat.class);
        assertEquals("2025-01-01", restored.created_at);
        assertEquals("enterprise", restored.plan_type);
    }
    @Test
    void createSchemaBuildsExpectedSchemaInfoAndFetchFields() {
        SchemaDefinition.Builder sb = GitHubCopilotSeatHandler.createSchema(null, null);
        SchemaDefinition schema = sb.build();

        assertEquals("GitHubCopilotSeat", schema.getType());

        ObjectClassInfo oci = schema.getObjectClassInfo();

        AttributeInfo uidInfo = findAttr(oci, Uid.NAME);
        AttributeInfo nameInfo = findAttr(oci, Name.NAME);

        assertEquals("id", uidInfo.getNativeName());
        assertFalse(uidInfo.isCreateable());
        assertFalse(uidInfo.isUpdateable());
        assertTrue(uidInfo.isReadable());

        assertEquals("displayName", nameInfo.getNativeName());
        assertTrue(nameInfo.isRequired());

        assertEquals("id", schema.getFetchField(Uid.NAME));
        assertNotNull(schema.getFetchField(Name.NAME));

        assertEquals("assigning_team.slug", schema.getFetchField("assigning_team.slug"));
        assertEquals("assignee.type", schema.getFetchField("assignee.type"));

        AttributeInfo createdAt = findAttr(oci, "created_at");
        assertFalse(createdAt.isCreateable());
        assertFalse(createdAt.isUpdateable());

        AttributeInfo updatedAt = findAttr(oci, "updated_at");
        assertFalse(updatedAt.isCreateable());
        assertFalse(updatedAt.isUpdateable());
    }

    @Test
    void createSchemaReadMappersAreExecutableViaToConnectorObjectBuilder() {
        SchemaDefinition schema = GitHubCopilotSeatHandler.createSchema(null, null).build();

        GitHubCopilotSeat seat = new GitHubCopilotSeat();
        seat.assignee = new GitHubCopilotSeatAssignee();
        seat.assignee.id = "a-123";
        seat.assignee.login = "jdoe";
        seat.assignee.type = "User";

        seat.assigning_team = new GitHubCopilotSeatAssigningTeam();
        seat.assigning_team.slug = "team-x";

        seat.created_at = "2024-01-01T10:30:20+00:00";
        seat.last_authenticated_at = "2024-01-02T10:20:30+00:00";
        seat.updated_at = "2024-01-03T10:20:30+00:00";
        seat.last_activity_at = "2024-01-04T10:20:30+00:00";
        seat.pending_cancellation_date = "2024-01-05";

        seat.last_activity_editor = "vim";
        seat.plan_type = "business";

        Set<String> attrsToGet = schema.getObjectClassInfo().getAttributeInfo().stream()
                .map(AttributeInfo::getName)
                .collect(Collectors.toSet());

        ConnectorObject co = schema.toConnectorObjectBuilder(seat, attrsToGet, false).build();

        assertEquals("a-123", co.getUid().getUidValue());
        assertEquals("jdoe", co.getName().getNameValue());

        assertEquals("vim", AttributeUtil.getStringValue(co.getAttributeByName("last_activity_editor")));
        assertEquals("business", AttributeUtil.getStringValue(co.getAttributeByName("plan_type")));
        assertEquals("User", AttributeUtil.getStringValue(co.getAttributeByName("assignee.type")));
        assertEquals("team-x", AttributeUtil.getStringValue(co.getAttributeByName("assigning_team.slug")));

        Object createdVal = AttributeUtil.getSingleValue(co.getAttributeByName("created_at"));
        assertNotNull(createdVal);
        assertTrue(createdVal instanceof ZonedDateTime);

        Object updatedVal = AttributeUtil.getSingleValue(co.getAttributeByName("updated_at"));
        assertNotNull(updatedVal);
        assertTrue(updatedVal instanceof ZonedDateTime);
    }

    @Test
    void createSchemaCreateMappersAreExecutableViaApply() {
        SchemaDefinition schema = GitHubCopilotSeatHandler.createSchema(null, null).build();

        GitHubCopilotSeat dest = new GitHubCopilotSeat();
        dest.assignee = new GitHubCopilotSeatAssignee();
        dest.assigning_team = new GitHubCopilotSeatAssigningTeam();

        Set<Attribute> attrs = new HashSet<>();
        attrs.add(AttributeBuilder.build(Name.NAME, "new-login"));
        attrs.add(AttributeBuilder.build("last_activity_editor", "nano"));
        attrs.add(AttributeBuilder.build("plan_type", "enterprise"));
        attrs.add(AttributeBuilder.build("assignee.type", "Bot"));
        attrs.add(AttributeBuilder.build("assigning_team.slug", "team-z"));

        schema.apply(attrs, dest);

        assertEquals("new-login", dest.assignee.login);
        assertEquals("nano", dest.last_activity_editor);
        assertEquals("enterprise", dest.plan_type);
        assertEquals("Bot", dest.assignee.type);
        assertEquals("team-z", dest.assigning_team.slug);
    }

    @Test
    void createSchemaAssigningTeamSlugCreateMapperIgnoresNullSource() {
        SchemaDefinition schema = GitHubCopilotSeatHandler.createSchema(null, null).build();

        GitHubCopilotSeat dest = new GitHubCopilotSeat();
        dest.assignee = new GitHubCopilotSeatAssignee();
        dest.assigning_team = new GitHubCopilotSeatAssigningTeam();
        dest.assigning_team.slug = "existing";

        Set<Attribute> attrs = Set.of(
                AttributeBuilder.build("assigning_team.slug", (String) null)
        );

        schema.apply(attrs, dest);

        assertEquals("existing", dest.assigning_team.slug);
    }

    private static AttributeInfo findAttr(ObjectClassInfo oci, String name) {
        return oci.getAttributeInfo().stream()
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("AttributeInfo not found: " + name));
    }
}
