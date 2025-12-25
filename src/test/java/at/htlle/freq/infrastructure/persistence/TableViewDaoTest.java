package at.htlle.freq.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TableViewDaoTest {

    private NamedParameterJdbcTemplate jdbc;
    private TableViewDao dao;

    @BeforeEach
    void setUp() {
        jdbc = mock(NamedParameterJdbcTemplate.class);
        dao = new TableViewDao(jdbc);
    }

    @Test
    void fetchTableAddsJoinsForUpgradePlans() {
        when(jdbc.queryForList(any(String.class), any(MapSqlParameterSource.class))).thenReturn(List.of());

        dao.fetchTable("UpgradePlan", 25);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).queryForList(sqlCaptor.capture(), paramsCaptor.capture());

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("FROM UpgradePlan"));
        assertTrue(sql.contains("JOIN Site"));
        assertTrue(sql.contains("JOIN Software"));
        assertEquals(25, paramsCaptor.getValue().getValue("limit"));
    }

    @Test
    void fetchTableFallsBackToStarSelect() {
        when(jdbc.queryForList(any(String.class), any(MapSqlParameterSource.class))).thenReturn(List.of());

        dao.fetchTable("Account", 5);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).queryForList(sqlCaptor.capture(), paramsCaptor.capture());

        assertEquals("SELECT * FROM Account LIMIT :limit", sqlCaptor.getValue());
        assertEquals(5, paramsCaptor.getValue().getValue("limit"));
    }
}

