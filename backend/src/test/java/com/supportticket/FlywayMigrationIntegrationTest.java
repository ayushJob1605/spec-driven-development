package com.supportticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigratesTicketAndCommentTables() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(tableExists(connection, "ticket")).isTrue();
            assertThat(tableExists(connection, "comment")).isTrue();
        }

        Integer ticketColumns = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE lower(table_name) = 'ticket'
                  AND lower(column_name) IN
                    ('id','title','description','priority','status','assignee','created_at','updated_at')
                """,
                Integer.class);
        assertThat(ticketColumns).isEqualTo(8);

        Integer commentColumns = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE lower(table_name) = 'comment'
                  AND lower(column_name) IN
                    ('id','ticket_id','content','author','created_at','updated_at')
                """,
                Integer.class);
        assertThat(commentColumns).isEqualTo(6);
    }

    private static boolean tableExists(Connection connection, String tableName) throws Exception {
        try (ResultSet tables = connection.getMetaData().getTables(null, null, tableName, new String[] {"TABLE"})) {
            if (tables.next()) {
                return true;
            }
        }
        try (ResultSet tables = connection.getMetaData().getTables(null, null, tableName.toUpperCase(), new String[] {"TABLE"})) {
            return tables.next();
        }
    }
}
