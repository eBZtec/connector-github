package jp.openstandia.connector.github;

import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubFilterTranslatorTest {

    private static OperationOptions opts() {
        return new OperationOptionsBuilder().build();
    }

    @Test
    void equalsExpression_onUid_returnsByUid() {
        GitHubFilterTranslator tr =
                new GitHubFilterTranslator(ObjectClass.ACCOUNT, opts());

        Uid uid = new Uid("123");
        GitHubFilter f = tr.createEqualsExpression(new EqualsFilter(uid), false);

        assertNotNull(f);
        assertTrue(f.isByUid());
        assertFalse(f.isByName());
    }

    @Test
    void equalsExpression_onName_returnsByName() {
        GitHubFilterTranslator tr =
                new GitHubFilterTranslator(ObjectClass.ACCOUNT, opts());

        Name name = new Name("alice");
        GitHubFilter f = tr.createEqualsExpression(new EqualsFilter(name), false);

        assertNotNull(f);
        assertTrue(f.isByName());
        assertFalse(f.isByUid());
    }

    @Test
    void equalsExpression_onUnsupportedAttr_returnsNull() {
        GitHubFilterTranslator tr =
                new GitHubFilterTranslator(ObjectClass.ACCOUNT, opts());

        Attribute other = AttributeBuilder.build("email", "a@b.c");
        GitHubFilter f = tr.createEqualsExpression(new EqualsFilter(other), false);

        assertNull(f);
    }

    @Test
    void equalsExpression_notFlag_returnsNull() {
        GitHubFilterTranslator tr =
                new GitHubFilterTranslator(ObjectClass.ACCOUNT, opts());

        GitHubFilter f = tr.createEqualsExpression(new EqualsFilter(new Name("x")), true);
        assertNull(f);
    }

    @Test
    void containsAll_onGroupWithMembersExactMatch_returnsByMembers() {
        // Usa o mesmo ObjectClass que o tradutor verifica
        GitHubFilterTranslator tr =
                new GitHubFilterTranslator(GitHubEMUGroupHandler.GROUP_OBJECT_CLASS, opts());

        Attribute members = AttributeBuilder.build("members.User.value", "u-001");
        GitHubFilter f =
                tr.createContainsAllValuesExpression(new ContainsAllValuesFilter(members), false);

        assertNotNull(f);
        assertTrue(f.isByMembers(), "Esperado filtro de membros (EXACT_MATCH em members.User.value)");
        assertEquals("members.User.value", f.attributeName);
        assertEquals(GitHubFilter.FilterType.EXACT_MATCH, f.filterType);
        assertSame(members, f.attributeValue);
    }

    @Test
    void containsAll_onDifferentObjectClass_returnsNull() {
        GitHubFilterTranslator tr =
                new GitHubFilterTranslator(ObjectClass.ACCOUNT, opts());

        Attribute members = AttributeBuilder.build("members.User.value", "u-001");
        GitHubFilter f =
                tr.createContainsAllValuesExpression(new ContainsAllValuesFilter(members), false);

        assertNull(f);
    }

    @Test
    void containsAll_onGroupButDifferentAttribute_returnsNull() {
        GitHubFilterTranslator tr =
                new GitHubFilterTranslator(GitHubEMUGroupHandler.GROUP_OBJECT_CLASS, opts());

        Attribute other = AttributeBuilder.build("members.Group.value", "g-1");
        GitHubFilter f =
                tr.createContainsAllValuesExpression(new ContainsAllValuesFilter(other), false);

        assertNull(f);
    }

    @Test
    void containsAll_notFlag_returnsNull() {
        GitHubFilterTranslator tr =
                new GitHubFilterTranslator(GitHubEMUGroupHandler.GROUP_OBJECT_CLASS, opts());

        Attribute members = AttributeBuilder.build("members.User.value", "u-001");
        GitHubFilter f =
                tr.createContainsAllValuesExpression(new ContainsAllValuesFilter(members), true);

        assertNull(f);
    }
}
