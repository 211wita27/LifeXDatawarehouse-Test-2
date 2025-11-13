package at.htlle.freq.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SiteControllerTest {

    private NamedParameterJdbcTemplate jdbc;
    private SiteController controller;

    @BeforeEach
    void setUp() {
        jdbc = mock(NamedParameterJdbcTemplate.class);
        controller = new SiteController(jdbc);
    }

    @Test
    void updateRejectsUnknownColumns() {
        Map<String, Object> body = Map.of("UnknownColumn", "value");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.update("SITE-1", body));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(jdbc, never()).update(anyString(), any(MapSqlParameterSource.class));
    }
}
