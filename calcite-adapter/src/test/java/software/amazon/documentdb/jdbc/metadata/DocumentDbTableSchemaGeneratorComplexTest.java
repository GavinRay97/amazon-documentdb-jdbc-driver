/*
 * Copyright <2021> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package software.amazon.documentdb.jdbc.metadata;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonNull;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.documentdb.jdbc.common.utilities.JdbcType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.calcite.sql.parser.SqlParser.DEFAULT_IDENTIFIER_MAX_LENGTH;
import static software.amazon.documentdb.jdbc.metadata.DocumentDbTableSchemaGeneratorHelper.combinePath;
import static software.amazon.documentdb.jdbc.metadata.DocumentDbTableSchemaGeneratorHelper.toName;

public class DocumentDbTableSchemaGeneratorComplexTest extends DocumentDbTableSchemaGeneratorTest {
    @Test
    void testDeepStructuredData() {
        final Map<String, String> tableNameMap = new HashMap<>();
        final String json = "{\n"
                + "    \"_id\" : \"60a2c0c65be86c8f6a007514\",\n"
                + "    \"field\" : \"string\",\n"
                + "    \"count\" : 19,\n"
                + "    \"timestamp\" : \"2021-05-17T19:15:18.316Z\",\n"
                + "    \"subDocument\" : {\n"
                + "        \"field\" : \"ABC\",\n"
                + "        \"field2\" : [\n"
                + "            \"A\",\n"
                + "            \"B\",\n"
                + "            \"C\"\n"
                + "        ]\n"
                + "    },\n"
                + "    \"twoLevelArray\" : [\n"
                + "        [\n"
                + "            1,\n"
                + "            2\n"
                + "        ],\n"
                + "        [\n"
                + "            3,\n"
                + "            4\n"
                + "        ],\n"
                + "        [\n"
                + "            5,\n"
                + "            6\n"
                + "        ]\n"
                + "    ],\n"
                + "    \"nestedArray\" : [\n"
                + "        {\n"
                + "            \"document\" : 0,\n"
                + "            \"innerArray\" : [\n"
                + "                1,\n"
                + "                2,\n"
                + "                3\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"document\" : 1,\n"
                + "            \"innerArray\" : [\n"
                + "                1,\n"
                + "                2,\n"
                + "                3\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"document\" : 2,\n"
                + "            \"innerArray\" : [\n"
                + "                1,\n"
                + "                2,\n"
                + "                3\n"
                + "            ]\n"
                + "        }\n"
                + "    ],\n"
                + "    \"nestedSubDocument\" : {\n"
                + "        \"field\" : 0,\n"
                + "        \"subDoc0\" : {\n"
                + "            \"field\" : 1,\n"
                + "            \"subDoc1\" : {\n"
                + "                \"field\" : 2\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
        final BsonDocument document = BsonDocument.parse(json);

        final Map<String, DocumentDbSchemaTable> metadata = DocumentDbTableSchemaGenerator
                .generate(COLLECTION_NAME, Collections.singleton(document).iterator());
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(9, metadata.size());

        final DocumentDbSchemaTable baseTable = metadata.get(COLLECTION_NAME);
        Assertions.assertNotNull(baseTable);
        Assertions.assertEquals(4, baseTable.getColumns().size());

        final DocumentDbSchemaTable subDocument = metadata
                .get(toName(combinePath(COLLECTION_NAME, "subDocument"), tableNameMap));
        Assertions.assertNotNull(subDocument);
        Assertions.assertEquals(2, subDocument.getColumns().size());

        final DocumentDbSchemaTable subDocumentField2 = metadata
                .get(toName(combinePath(combinePath(
                        COLLECTION_NAME, "subDocument"), "field2"), tableNameMap));
        Assertions.assertNotNull(subDocumentField2);
        Assertions.assertEquals(3, subDocumentField2.getColumns().size());

        final DocumentDbSchemaTable twoLevelArray = metadata
                .get(toName(combinePath(
                        COLLECTION_NAME, "twoLevelArray"), tableNameMap));
        Assertions.assertNotNull(twoLevelArray);
        Assertions.assertEquals(4, twoLevelArray.getColumns().size());

        final DocumentDbSchemaTable nestedArray = metadata
                .get(toName(combinePath(
                        COLLECTION_NAME, "nestedArray"), tableNameMap));
        Assertions.assertNotNull(nestedArray);
        Assertions.assertEquals(3, nestedArray.getColumns().size());

        final DocumentDbSchemaTable nestedArrayInnerArray = metadata
                .get(toName(combinePath(combinePath(
                        COLLECTION_NAME, "nestedArray"), "innerArray"), tableNameMap));
        Assertions.assertNotNull(nestedArrayInnerArray);
        Assertions.assertEquals(4, nestedArrayInnerArray.getColumns().size());

        final DocumentDbSchemaTable nestedSubDocument = metadata
                .get(toName(combinePath(
                        COLLECTION_NAME, "nestedSubDocument"), tableNameMap));
        Assertions.assertNotNull(nestedSubDocument);
        Assertions.assertEquals(2, nestedSubDocument.getColumns().size());
        final DocumentDbSchemaTable subDoc0 = metadata
                .get(toName(combinePath(combinePath(
                        COLLECTION_NAME, "nestedSubDocument"), "subDoc0"), tableNameMap));
        Assertions.assertNotNull(subDoc0);
        final DocumentDbSchemaTable subDoc1 = metadata
                .get(toName(combinePath(combinePath(combinePath(
                        COLLECTION_NAME, "nestedSubDocument"), "subDoc0"), "subDoc1"), tableNameMap));
        Assertions.assertNotNull(subDoc1);
    }

    @DisplayName("Test whether a sub-document with '_id' as a field is handled correctly.")
    @Test
    void testNestedDocumentWithIdInSubDocument() {
        final String nestedJson = "{\n"
                + "  \"_id\": { \"$oid\": \"607d96b40352ee001f493a73\" },\n"
                + "  \"language\": \"en\",\n"
                + "  \"tags\": [],\n"
                + "  \"title\": \"TT Eval\",\n"
                + "  \"name\": \"tt_eval\",\n"
                + "  \"type\": \"form\",\n"
                + "  \"created\": \"2021-04-19T14:41:56.252Z\",\n"
                + "  \"modified\": \"2021-04-19T14:41:56.252Z\",\n"
                + "  \"owner\": \"12345\",\n"
                + "  \"components\": [\n"
                + "    {\n"
                + "      \"_id\": { \"$oid\": \"607d96b40352ee001f493aca\" },\n"
                + "      \"label\": \"Objective\",\n"
                + "      \"required\": false,\n"
                + "      \"tooltip\": \"additional note go here to describe context of question 54\",\n"
                + "      \"name\": \"tteval-54\",\n"
                + "      \"type\": \"section\",\n"
                + "      \"components\": [\n"
                + "        {\n"
                + "          \"_id\": { \"$oid\": \"607d96b50352ee001f493bdc\" },\n"
                + "          \"label\": \"Strength/ROM\",\n"
                + "          \"required\": false,\n"
                + "          \"tooltip\": \"additional note go here to describe context of question 221\",\n"
                + "          \"name\": \"tteval-221\",\n"
                + "          \"type\": \"section\",\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  ],\n"
                + "  \"__v\": { \"$numberInt\": \"0\" }\n"
                + "}\n";
        final BsonDocument document = BsonDocument.parse(nestedJson);
        final Map<String, DocumentDbSchemaTable> metadata = DocumentDbTableSchemaGenerator
                .generate(COLLECTION_NAME, Collections.singleton(document).iterator());
        Assertions.assertNotNull(metadata);
    }

    @DisplayName("Tests when some arrays are empty")
    @Test
    void testEmptyNonEmptyObjectArray() {
        final Map<String, String> tableNameMap = new HashMap<>();
        final String nestedJson = "{\n"
                + "  \"_id\": { \"$oid\": \"607d96b40352ee001f493a73\" },\n"
                + "  \"language\": \"en\",\n"
                + "  \"tags\": [],\n"
                + "  \"title\": \"TT Eval\",\n"
                + "  \"name\": \"tt_eval\",\n"
                + "  \"type\": \"form\",\n"
                + "  \"created\": \"2021-04-19T14:41:56.252Z\",\n"
                + "  \"modified\": \"2021-04-19T14:41:56.252Z\",\n"
                + "  \"owner\": \"12345\",\n"
                + "  \"array\": [\n"
                + "    {\n"
                + "      \"components\": []\n"
                + "    },\n"
                + "    {\n"
                + "      \"components\": [\n"
                + "        {\n"
                + "          \"_id\": { \"$oid\": \"607d96b40352ee001f493acb\" },\n"
                + "          \"label\": \"Objective b\",\n"
                + "          \"required\": false,\n"
                + "          \"tooltip\": \"additional note go here to describe context of question b\",\n"
                + "          \"name\": \"tteval-b\",\n"
                + "          \"type\": \"section\"\n"
                + "        }\n"
                + "      ]\n"
                + "    },\n"
                + "    {\n"
                + "      \"components\": []\n"
                + "    }\n"
                + "  ],\n"
                + "  \"__v\": { \"$numberInt\": \"0\" }\n"
                + "}\n";
        final BsonDocument document = BsonDocument.parse(nestedJson);
        final Map<String, DocumentDbSchemaTable> metadata = DocumentDbTableSchemaGenerator
                .generate(COLLECTION_NAME, Collections.singleton(document).iterator());
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(4, metadata.size());

        DocumentDbSchemaTable schemaTable = metadata.get(COLLECTION_NAME);
        Assertions.assertNotNull(schemaTable);
        Assertions.assertEquals(9, schemaTable.getColumns().size());

        schemaTable = metadata.get(toName(combinePath(COLLECTION_NAME, "tags"), tableNameMap));
        Assertions.assertNotNull(schemaTable);
        Assertions.assertEquals(3, schemaTable.getColumns().size());
        Assertions
                .assertEquals(JdbcType.NULL, schemaTable.getColumnMap().get("value").getSqlType());

        schemaTable = metadata.get(toName(combinePath(COLLECTION_NAME, "array"), tableNameMap));
        Assertions.assertNotNull(schemaTable);
        Assertions.assertEquals(2, schemaTable.getColumns().size());

        schemaTable = metadata.get(toName(combinePath(combinePath(
                COLLECTION_NAME, "array"), "components"), tableNameMap));
        Assertions.assertNotNull(schemaTable);
        Assertions.assertEquals(9, schemaTable.getColumns().size());
        Assertions
                .assertEquals(JdbcType.VARCHAR, schemaTable.getColumnMap().get("_id").getSqlType());
        Assertions.assertEquals(BsonType.OBJECT_ID,
                schemaTable.getColumnMap().get("_id").getDbType());
        Assertions.assertEquals(JdbcType.BOOLEAN,
                schemaTable.getColumnMap().get("required").getSqlType());
        Assertions.assertEquals(BsonType.BOOLEAN,
                schemaTable.getColumnMap().get("required").getDbType());
        Assertions.assertNull(schemaTable.getColumnMap().get("value"));
    }

    @DisplayName("Tests that even deeply nested documents and array have name length less than max.")
    @Test
    void testDeeplyNestedDocumentsArraysForSqlNameLength() {
        BsonValue doc = new BsonNull();
        for (int i = 199; i >= 0; i--) {
            doc = new BsonDocument("_id", new BsonInt32(i))
                    .append(i + "field", new BsonInt32(i))
                    .append(i + "doc", doc)
                    .append(i + "array", new BsonArray(Collections.singletonList(new BsonInt32(i))));
        }
        final Map<String, DocumentDbSchemaTable> tableMap = DocumentDbTableSchemaGenerator
                .generate(COLLECTION_NAME, Collections.singleton((BsonDocument) doc).iterator());

        Assertions.assertEquals(400, tableMap.size());
        tableMap.keySet().stream()
                .map(tableName -> tableName.length() <= DEFAULT_IDENTIFIER_MAX_LENGTH)
                .forEach(Assertions::assertTrue);
        tableMap.values().stream()
                .flatMap(schemaTable -> schemaTable.getColumns().stream())
                .map(schemaColumn -> schemaColumn.getSqlName().length()
                        <= DEFAULT_IDENTIFIER_MAX_LENGTH)
                .forEach(Assertions::assertTrue);
    }

    @DisplayName("Tests that sub-document of array is not present on first document, still should have index column.")
    @Test
    void testSubDocumentInArrayOnlyInSecondDocument() {
        final List<BsonDocument> docs = new ArrayList<>();
        String documentString;
        documentString = "{\"_id\":{\"$oid\":\"6050ea8da110dd2c5f279bd0\"},\"data\":{\"_id\":{\"$oid\":\"6050ea8da110dd2c5f279bcf\"},\"locations\":[{\"_id\":\"06c782fe-b89e-43b1-8748-b671ad7d3ad9\"}]}}";
        docs.add(BsonDocument.parse(documentString));
        documentString = "{\"_id\":{\"$oid\":\"6050ea8ea110dd2c5f279bd4\"},\"data\":{\"_id\":{\"$oid\":\"6050ea8ea110dd2c5f279bd3\"},\"locations\":[{\"_id\":\"06c782fe-b89e-43b1-8748-b671ad7d3ad9\",\"nonRegisteredBranch\":{\"_id\":\"06c782fe-b89e-43b1-8748-b671ad7d3ad9\"}}]}}";
        docs.add(BsonDocument.parse(documentString));

        final Map<String, DocumentDbSchemaTable> tableMap = DocumentDbTableSchemaGenerator
                .generate("employmentHistory", docs.iterator());
        Assertions.assertEquals(4, tableMap.size());
        final DocumentDbSchemaTable table1 = tableMap.get("employmentHistory_data_locations");
        Assertions.assertNotNull(table1);
        Assertions.assertNotNull(table1.getColumnMap().get("data_locations_index_lvl_0"));
        final DocumentDbSchemaTable table2 = tableMap.get(
                "employmentHistory_data_locations_nonRegisteredBranch");
        Assertions.assertNotNull(table2);
        Assertions.assertNotNull(table2.getColumnMap().get("data_locations_index_lvl_0"));
    }

    @DisplayName("Tests an array with multiple documents and nulls.")
    @Test
    void testArrayWithDocumentsAndNull() {
        final Map<String, String> tableNameMap = new HashMap<>();
        final BsonDocument document = BsonDocument.parse(
                "{ \"_id\" : \"key\", \"array\" : [ { \"field\" : 1 }, null, { \"field\": 2}, null]}");
        final Map<String, DocumentDbSchemaTable> metadata = DocumentDbTableSchemaGenerator
                .generate(COLLECTION_NAME, Arrays.stream((new BsonDocument[]{document})).iterator());
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(2, metadata.size());

        // Virtual table for array of documents
        final DocumentDbMetadataTable metadataTable = (DocumentDbMetadataTable) metadata.get(toName(combinePath(
                COLLECTION_NAME, "array"), tableNameMap));
        Assertions.assertEquals(3, metadataTable.getColumnMap().size());

        // _id foreign key column
        DocumentDbMetadataColumn metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath(COLLECTION_NAME, "_id"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.VARCHAR, metadataColumn.getSqlType());
        Assertions.assertEquals("_id",metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath(COLLECTION_NAME, "_id"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(1, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertEquals(1, metadataColumn.getForeignKeyIndex());
        Assertions.assertFalse(metadataColumn.isGenerated());

        // index key column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath("array", "index_lvl_0"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.BIGINT, metadataColumn.getSqlType());
        Assertions.assertEquals("array", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath("array", "index_lvl_0"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(2, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertTrue(metadataColumn.isGenerated());

        // field column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get("field");
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.INTEGER, metadataColumn.getSqlType());
        Assertions.assertEquals(combinePath("array", "field"), metadataColumn.getFieldPath());
        Assertions.assertEquals("field", metadataColumn.getSqlName());
        Assertions.assertEquals(0, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertFalse(metadataColumn.isGenerated());

        printMetadataOutput(metadata, getMethodName());
    }

    @DisplayName("Tests a two-level scalar array with nulls on both levels.")
    @Test
    void testTwoLevelArrayWithNulls() {
        final Map<String, String> tableNameMap = new HashMap<>();
        final BsonDocument document = BsonDocument.parse(
                "{ \"_id\" : \"key\", \"array\" : [ [1, 2, 3 ], [], [ 4, 5, 6, null ], null]}");
        final Map<String, DocumentDbSchemaTable> metadata = DocumentDbTableSchemaGenerator
                .generate(COLLECTION_NAME, Arrays.stream((new BsonDocument[]{document})).iterator());
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(2, metadata.size());

        // Virtual table for 2 level nested array
        final DocumentDbMetadataTable metadataTable = (DocumentDbMetadataTable) metadata.get(
                toName(combinePath(COLLECTION_NAME, "array"), tableNameMap));
        Assertions.assertEquals(4, metadataTable.getColumnMap().size());

        // _id foreign key column
        DocumentDbMetadataColumn metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath(COLLECTION_NAME, "_id"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.VARCHAR, metadataColumn.getSqlType());
        Assertions.assertEquals("_id", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath(COLLECTION_NAME, "_id"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(1, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(1, metadataColumn.getForeignKeyIndex());
        Assertions.assertEquals(COLLECTION_NAME, metadataColumn.getForeignKeyTableName());
        Assertions.assertEquals(COLLECTION_NAME + "__id", metadataColumn.getForeignKeyColumnName());
        Assertions.assertFalse(metadataColumn.isGenerated());

        // index key column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath("array", "index_lvl_0"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.BIGINT, metadataColumn.getSqlType());
        Assertions.assertEquals("array", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath("array", "index_lvl_0"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(2, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertNull(metadataColumn.getForeignKeyColumnName());
        Assertions.assertNull(metadataColumn.getForeignKeyTableName());
        Assertions.assertTrue(metadataColumn.isGenerated());

        // index key column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath("array", "index_lvl_1"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.BIGINT, metadataColumn.getSqlType());
        Assertions.assertEquals("array", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath("array", "index_lvl_1"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(3, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertNull(metadataColumn.getForeignKeyColumnName());
        Assertions.assertNull(metadataColumn.getForeignKeyTableName());
        Assertions.assertTrue(metadataColumn.isGenerated());

        // value key column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get("value");
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.INTEGER, metadataColumn.getSqlType());
        Assertions.assertEquals("array", metadataColumn.getFieldPath());
        Assertions.assertEquals("value", metadataColumn.getSqlName());
        Assertions.assertEquals(0, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertFalse(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertNull(metadataColumn.getForeignKeyColumnName());
        Assertions.assertNull(metadataColumn.getForeignKeyTableName());
        Assertions.assertFalse(metadataColumn.isGenerated());

        printMetadataOutput(metadata, getMethodName());
    }

    @DisplayName("Tests a two-level array of documents with nulls in both levels.")
    @Test
    void testTwoLevelDocumentArrayWithNulls() {
        final Map<String, String> tableNameMap = new HashMap<>();
        final BsonDocument document = BsonDocument.parse("{ \"_id\" : \"key\", \"array\" : [ [ {\"field\": 1}, {\"field\": null}, null], null]}");
        final Map<String, DocumentDbSchemaTable> metadata = DocumentDbTableSchemaGenerator
                .generate(COLLECTION_NAME, Arrays.stream((new BsonDocument[]{document})).iterator());
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(2, metadata.size());

        // Virtual table for 2 level nested array of documents
        final DocumentDbMetadataTable metadataTable = (DocumentDbMetadataTable) metadata.get(
                toName(combinePath(COLLECTION_NAME, "array"), tableNameMap));
        Assertions.assertEquals(4, metadataTable.getColumnMap().size());

        // _id foreign key column
        DocumentDbMetadataColumn metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath(COLLECTION_NAME, "_id"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.VARCHAR, metadataColumn.getSqlType());
        Assertions.assertEquals("_id", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath(COLLECTION_NAME, "_id"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(1, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(1, metadataColumn.getForeignKeyIndex());
        Assertions.assertEquals(COLLECTION_NAME, metadataColumn.getForeignKeyTableName());
        Assertions.assertEquals(COLLECTION_NAME + "__id", metadataColumn.getForeignKeyColumnName());
        Assertions.assertFalse(metadataColumn.isGenerated());

        // index key column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath("array", "index_lvl_0"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.BIGINT, metadataColumn.getSqlType());
        Assertions.assertEquals("array", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath("array", "index_lvl_0"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(2, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertNull(metadataColumn.getForeignKeyColumnName());
        Assertions.assertNull(metadataColumn.getForeignKeyTableName());
        Assertions.assertTrue(metadataColumn.isGenerated());

        // index key column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath("array", "index_lvl_1"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.BIGINT, metadataColumn.getSqlType());
        Assertions.assertEquals("array", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath("array", "index_lvl_1"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(3, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertNull(metadataColumn.getForeignKeyColumnName());
        Assertions.assertNull(metadataColumn.getForeignKeyTableName());
        Assertions.assertTrue(metadataColumn.isGenerated());

        // field column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get("field");
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.INTEGER, metadataColumn.getSqlType());
        Assertions.assertEquals(combinePath("array", "field"), metadataColumn.getFieldPath());
        Assertions.assertEquals("field", metadataColumn.getSqlName());
        Assertions.assertEquals(0, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertFalse(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertNull(metadataColumn.getForeignKeyColumnName());
        Assertions.assertNull(metadataColumn.getForeignKeyTableName());
        Assertions.assertFalse(metadataColumn.isGenerated());

        printMetadataOutput(metadata, getMethodName());
    }

    @DisplayName("Tests an array of documents with array values and nulls in all levels.")
    @Test
    void testDocumentArrayWithNulls() {
        final Map<String, String> tableNameMap = new HashMap<>();
        final BsonDocument document = BsonDocument.parse("{ \"_id\" : \"key\", \"array\" : [ {\"field\": [1, null]}, {\"field\": null}, null]}");
        final Map<String, DocumentDbSchemaTable> metadata = DocumentDbTableSchemaGenerator
                .generate(COLLECTION_NAME, Arrays.stream((new BsonDocument[]{document})).iterator());
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(3, metadata.size());

        // Virtual table for array_field
        final DocumentDbMetadataTable metadataTable = (DocumentDbMetadataTable) metadata.get(
                toName(combinePath(COLLECTION_NAME, "array_field"), tableNameMap));
        Assertions.assertEquals(4, metadataTable.getColumnMap().size());

        // _id foreign key column
        DocumentDbMetadataColumn metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath(COLLECTION_NAME, "_id"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.VARCHAR, metadataColumn.getSqlType());
        Assertions.assertEquals("_id", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath(COLLECTION_NAME, "_id"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(1, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(1, metadataColumn.getForeignKeyIndex());
        Assertions.assertEquals(COLLECTION_NAME, metadataColumn.getForeignKeyTableName());
        Assertions.assertEquals(COLLECTION_NAME + "__id", metadataColumn.getForeignKeyColumnName());
        Assertions.assertFalse(metadataColumn.isGenerated());

        // index key column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath("array", "index_lvl_0"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.BIGINT, metadataColumn.getSqlType());
        Assertions.assertEquals("array", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath("array", "index_lvl_0"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(2, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(2, metadataColumn.getForeignKeyIndex());
        Assertions.assertEquals(
                toName(combinePath("array", "index_lvl_0"), tableNameMap),
                metadataColumn.getForeignKeyColumnName());
        Assertions.assertEquals(
                toName(combinePath(COLLECTION_NAME, "array"), tableNameMap),
                metadataColumn.getForeignKeyTableName());
        Assertions.assertTrue(metadataColumn.isGenerated());

        // index key column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get(
                toName(combinePath("array_field", "index_lvl_0"), tableNameMap));
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.BIGINT, metadataColumn.getSqlType());
        Assertions.assertEquals("array.field", metadataColumn.getFieldPath());
        Assertions.assertEquals(
                toName(combinePath("array_field", "index_lvl_0"), tableNameMap),
                metadataColumn.getSqlName());
        Assertions.assertEquals(3, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertTrue(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertNull(metadataColumn.getForeignKeyColumnName());
        Assertions.assertNull(metadataColumn.getForeignKeyTableName());
        Assertions.assertTrue(metadataColumn.isGenerated());

        // value column
        metadataColumn = (DocumentDbMetadataColumn) metadataTable.getColumnMap().get("value");
        Assertions.assertNotNull(metadataColumn);
        Assertions.assertEquals(JdbcType.INTEGER, metadataColumn.getSqlType());
        Assertions.assertEquals(combinePath("array", "field"), metadataColumn.getFieldPath());
        Assertions.assertEquals("value", metadataColumn.getSqlName());
        Assertions.assertEquals(0, metadataColumn.getPrimaryKeyIndex());
        Assertions.assertFalse(metadataColumn.isPrimaryKey());
        Assertions.assertEquals(0, metadataColumn.getForeignKeyIndex());
        Assertions.assertNull(metadataColumn.getForeignKeyColumnName());
        Assertions.assertNull(metadataColumn.getForeignKeyTableName());
        Assertions.assertFalse(metadataColumn.isGenerated());

        printMetadataOutput(metadata, getMethodName());
    }
}
