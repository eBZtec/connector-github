package util;

import jp.openstandia.connector.util.SchemaDefinition;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class SchemaDefinitionTest {

    @Test
    void testNewBuilderOverload() {
        ObjectClass objectClass = new ObjectClass("TestClass");

        SchemaDefinition.Builder<String, String, Integer> builder =
                SchemaDefinition.newBuilder(objectClass, String.class, Integer.class);

        // Assert
        assertNotNull(builder);
    }

    @Test
    void testAddUidShouldCreateAndAddAttributeMapper() throws Exception {
        // Arrange
        ObjectClass objectClass = new ObjectClass("testClass");

        // Cria o builder com tipos genéricos simples
        SchemaDefinition.Builder<String, String, String> builder =
                new SchemaDefinition.Builder<>(objectClass, String.class, String.class, String.class);

        // Lambdas dummy (só pra satisfazer os parâmetros)
        BiConsumer<String, String> create = (value, obj) -> {};
        BiConsumer<String, String> update = (value, obj) -> {};
        Function<String, String> read = s -> "valor-" + s;

        // Act
        builder.addUid(
                "uidField",
                SchemaDefinition.Types.STRING,
                create,
                update,
                read,
                "fetchUid",
                AttributeInfo.Flags.REQUIRED
        );

        // Assert
        // Acessa o campo privado 'attributes'
        Field field = builder.getClass().getDeclaredField("attributes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> attributes = (List<?>) field.get(builder);

        assertEquals(1, attributes.size(), "Deveria conter 1 AttributeMapper");
        Object attr = attributes.get(0);
        assertNotNull(attr, "AttributeMapper não deveria ser nulo");

        // Verifica os campos internos via reflexão
        Field connectorNameField = attr.getClass().getDeclaredField("connectorName");
        connectorNameField.setAccessible(true);
        assertEquals("__UID__", connectorNameField.get(attr));

        Field nameField = attr.getClass().getDeclaredField("name");
        nameField.setAccessible(true);
        assertEquals("uidField", nameField.get(attr));

        Field fetchField = attr.getClass().getDeclaredField("fetchField");
        fetchField.setAccessible(true);
        assertEquals("fetchUid", fetchField.get(attr));

        Method isReadableAttributes = builder.getClass().getDeclaredMethod("isReadableAttributes");
    }

    @Test
    void testAddNameShouldCreateAndAddAttributeMapper() throws Exception {
        // Arrange
        ObjectClass objectClass = new ObjectClass("testClass");

        // Cria o builder com tipos genéricos simples
        SchemaDefinition.Builder<String, String, String> builder =
                new SchemaDefinition.Builder<>(objectClass, String.class, String.class, String.class);

        // Lambdas dummy
        BiConsumer<String, String> createOrUpdate = (value, obj) -> {};
        Function<String, String> read = s -> "name-" + s;

        // Act
        builder.addName(
                "displayName",
                SchemaDefinition.Types.STRING,
                createOrUpdate,
                read,
                "fetchName",
                AttributeInfo.Flags.NOT_UPDATEABLE
        );

        // Assert
        // Acessa o campo privado 'attributes'
        Field field = builder.getClass().getDeclaredField("attributes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> attributes = (List<?>) field.get(builder);

        assertEquals(1, attributes.size(), "Deveria conter 1 AttributeMapper");
        Object attr = attributes.get(0);
        assertNotNull(attr, "AttributeMapper não deveria ser nulo");

        // Verifica se o campo 'connectorName' é __NAME__
        Field connectorNameField = attr.getClass().getDeclaredField("connectorName");
        connectorNameField.setAccessible(true);
        assertEquals(Name.NAME, connectorNameField.get(attr));

        // Verifica o nome do atributo passado
        Field nameField = attr.getClass().getDeclaredField("name");
        nameField.setAccessible(true);
        assertEquals("displayName", nameField.get(attr));

        // Verifica o campo 'fetchField'
        Field fetchField = attr.getClass().getDeclaredField("fetchField");
        fetchField.setAccessible(true);
        assertEquals("fetchName", fetchField.get(attr));
    }

    @Test
    void testAddEnableShouldCreateAndAddAttributeMapper() throws Exception {
        // Arrange
        ObjectClass objectClass = new ObjectClass("testClass");

        SchemaDefinition.Builder<String, String, String> builder =
                new SchemaDefinition.Builder<>(objectClass, String.class, String.class, String.class);

        // Lambdas dummy
        BiConsumer<String, String> create = (value, obj) -> {};
        BiConsumer<String, String> update = (value, obj) -> {};
        Function<String, String> read = s -> "enabled-" + s;

        // Act
        builder.addEnable(
                "enabledFlag",
                SchemaDefinition.Types.STRING,
                create,
                update,
                read,
                "fetchEnable",
                AttributeInfo.Flags.NOT_CREATABLE
        );


        // Assert
        // Acessa o campo privado 'attributes'
        Field field = builder.getClass().getDeclaredField("attributes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> attributes = (List<?>) field.get(builder);

        assertEquals(1, attributes.size(), "Deveria conter 1 AttributeMapper");
        Object attr = attributes.get(0);
        assertNotNull(attr, "AttributeMapper não deveria ser nulo");

        // Verifica se o campo 'connectorName' é __ENABLE__
        Field connectorNameField = attr.getClass().getDeclaredField("connectorName");
        connectorNameField.setAccessible(true);
        assertEquals(OperationalAttributes.ENABLE_NAME, connectorNameField.get(attr));

        // Verifica o nome do atributo passado
        Field nameField = attr.getClass().getDeclaredField("name");
        nameField.setAccessible(true);
        assertEquals("enabledFlag", nameField.get(attr));

        // Verifica o campo 'fetchField'
        Field fetchField = attr.getClass().getDeclaredField("fetchField");
        fetchField.setAccessible(true);
        assertEquals("fetchEnable", fetchField.get(attr));
    }

    @Test
    void testBuildSchemaInfo_UUID() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.UUID, AttributeInfo.Flags.REQUIRED);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_STRING_CASE_IGNORE() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.STRING_CASE_IGNORE, AttributeInfo.Flags.NOT_CREATABLE);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_STRING_URI() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.STRING_URI, AttributeInfo.Flags.NOT_UPDATEABLE);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_STRING_LDAP_DN() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.STRING_LDAP_DN,
                AttributeInfo.Flags.NOT_READABLE, AttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_XML() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.XML, AttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);
        assertNotNull(info);
    }

    @Test
    void testBuildSchemaInfo_JSON() throws Exception {
        ObjectClassInfo info = buildForType(SchemaDefinition.Types.JSON);
        assertNotNull(info);
    }

    @SuppressWarnings("unchecked")
    private ObjectClassInfo buildForType(SchemaDefinition.Types<?> type, AttributeInfo.Flags... flags) throws Exception {
        ObjectClass objectClass = new ObjectClass("testClass");
        SchemaDefinition.Builder<String, String, String> builder =
                new SchemaDefinition.Builder<>(objectClass, String.class, String.class, String.class);

        Field field = builder.getClass().getDeclaredField("attributes");
        field.setAccessible(true);
        List<Object> attributes = (List<Object>) field.get(builder);

        Constructor<?> ctor = Class.forName("jp.openstandia.connector.util.SchemaDefinition$AttributeMapper").getDeclaredConstructor(
                String.class, String.class, SchemaDefinition.Types.class,
                BiConsumer.class, BiConsumer.class,
                Function.class, String.class, AttributeInfo.Flags[].class
        );
        ctor.setAccessible(true);

        attributes.add(ctor.newInstance(
                "attr_" + type.hashCode(),
                "attr_" + type.hashCode(),
                type,
                null, null, null,
                "fetch_" + type.hashCode(),
                flags.length == 0 ? new AttributeInfo.Flags[]{} : flags
        ));

        Method method = builder.getClass().getDeclaredMethod("buildSchemaInfo");
        method.setAccessible(true);
        return (ObjectClassInfo) method.invoke(builder);
    }
}

