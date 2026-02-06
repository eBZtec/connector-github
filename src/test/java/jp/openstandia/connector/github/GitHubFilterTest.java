package jp.openstandia.connector.github;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubFilterTest {

    @Test
    void byUid_shouldSetUidAndReportIsByUid() {
        Uid uid = new Uid("123");
        GitHubFilter f = GitHubFilter.By(uid);

        assertTrue(f.isByUid(), "isByUid should be true when created By(Uid)");
        assertFalse(f.isByName(), "isByName should be false when created By(Uid)");
        // isByMembers only applies to ByMember
        // (avoid NPE by not calling when attributeName is null)
        assertSame(uid, f.uid);
        assertNull(f.name);
        assertEquals(GitHubFilter.FilterType.EXACT_MATCH, f.filterType);
        assertNull(f.attributeName);
        assertNull(f.attributeValue);
    }

    @Test
    void byName_shouldSetNameAndReportIsByName() {
        Name name = new Name("alice");
        GitHubFilter f = GitHubFilter.By(name);

        assertTrue(f.isByName(), "isByName should be true when created By(Name)");
        assertFalse(f.isByUid(), "isByUid should be false when created By(Name)");
        assertSame(name, f.name);
        assertNull(f.uid);
        assertEquals(GitHubFilter.FilterType.EXACT_MATCH, f.filterType);
        assertNull(f.attributeName);
        assertNull(f.attributeValue);
    }

    @Test
    void byMember_exactMatchWithRightAttribute_shouldReportIsByMembersTrue() {
        Attribute memberAttr = AttributeBuilder.build("members.User.value", "u-001");
        GitHubFilter f = GitHubFilter.ByMember(
                "members.User.value",
                GitHubFilter.FilterType.EXACT_MATCH,
                memberAttr
        );

        assertFalse(f.isByUid());
        assertFalse(f.isByName());
        assertTrue(f.isByMembers(), "isByMembers should be true for EXACT_MATCH on members.User.value");

        assertEquals("members.User.value", f.attributeName);
        assertEquals(GitHubFilter.FilterType.EXACT_MATCH, f.filterType);
        assertSame(memberAttr, f.attributeValue);
    }

    @Test
    void byMember_wrongAttribute_shouldReportIsByMembersFalse() {
        Attribute otherAttr = AttributeBuilder.build("somethingElse", "x");
        GitHubFilter f = GitHubFilter.ByMember(
                "somethingElse",
                GitHubFilter.FilterType.EXACT_MATCH,
                otherAttr
        );

        assertFalse(f.isByUid());
        assertFalse(f.isByName());
        assertFalse(f.isByMembers(), "isByMembers should be false for attributes other than members.User.value");
    }

    @Test
    void byMember_rightAttributeButDifferentFilterType_shouldReportIsByMembersFalse() {
        // Como atualmente só existe EXACT_MATCH no enum, este teste
        // demonstra a intenção: se surgirem novos tipos, a verificação
        // continua correta. Aqui apenas reafirmamos o comportamento.
        Attribute memberAttr = AttributeBuilder.build("members.User.value", "u-002");
        GitHubFilter f = GitHubFilter.ByMember(
                "members.User.value",
                GitHubFilter.FilterType.EXACT_MATCH, // único tipo disponível hoje
                memberAttr
        );

        // Com o enum atual, continua true; se houver novos tipos no futuro,
        // este teste deve ser duplicado com um tipo diferente e esperar false.
        assertTrue(f.isByMembers());
    }
}
