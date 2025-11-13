package at.htlle.freq.application.report;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    @Test
    void configurationReportAggregatesBrowserClientsSeparately() throws SQLException {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        ReportService service = new ReportService(jdbc);

        ResultSet firstRow = mock(ResultSet.class);
        when(firstRow.getString("SiteName")).thenReturn("Alpha");
        when(firstRow.getString("ProjectName")).thenReturn("Project A");
        when(firstRow.getString("ProjectSAPID")).thenReturn("SAP-A");
        when(firstRow.getString("VariantCode")).thenReturn("V1");
        when(firstRow.getInt("server_count")).thenReturn(2);
        when(firstRow.getInt("ha_servers")).thenReturn(1);
        when(firstRow.getInt("client_count")).thenReturn(10);
        when(firstRow.getInt("local_clients")).thenReturn(4);
        when(firstRow.getInt("browser_clients")).thenReturn(3);
        when(firstRow.getInt("radio_count")).thenReturn(0);
        when(firstRow.getInt("audio_count")).thenReturn(2);
        when(firstRow.getInt("phone_count")).thenReturn(1);
        when(firstRow.getString("server_os")).thenReturn("Linux");
        when(firstRow.getString("client_os")).thenReturn("Windows");

        ResultSet secondRow = mock(ResultSet.class);
        when(secondRow.getString("SiteName")).thenReturn("Beta");
        when(secondRow.getString("ProjectName")).thenReturn("Project B");
        when(secondRow.getString("ProjectSAPID")).thenReturn("SAP-B");
        when(secondRow.getString("VariantCode")).thenReturn("V2");
        when(secondRow.getInt("server_count")).thenReturn(1);
        when(secondRow.getInt("ha_servers")).thenReturn(0);
        when(secondRow.getInt("client_count")).thenReturn(5);
        when(secondRow.getInt("local_clients")).thenReturn(2);
        when(secondRow.getInt("browser_clients")).thenReturn(1);
        when(secondRow.getInt("radio_count")).thenReturn(0);
        when(secondRow.getInt("audio_count")).thenReturn(0);
        when(secondRow.getInt("phone_count")).thenReturn(0);
        when(secondRow.getString("server_os")).thenReturn("Windows");
        when(secondRow.getString("client_os")).thenReturn("macOS");

        when(jdbc.query(anyString(), anyMap(), any(RowMapper.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            RowMapper<Map<String, Object>> mapper = (RowMapper<Map<String, Object>>) invocation.getArgument(2);
            List<Map<String, Object>> mappedRows = new ArrayList<>();
            try {
                mappedRows.add(mapper.mapRow(firstRow, 0));
                mappedRows.add(mapper.mapRow(secondRow, 1));
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return mappedRows;
        });

        ReportFilter filter = new ReportFilter(ReportType.CONFIGURATION, null, null, null, null, null, null);
        ReportResponse response = service.getReport(filter);

        assertEquals("LOCAL 6 · BROWSER 4", response.kpis().get(2).hint());

        ChartSlice browserSlice = response.chart().stream()
                .filter(slice -> "BROWSER".equals(slice.label()))
                .findFirst()
                .orElse(null);
        assertNotNull(browserSlice, "Browser slice missing");
        assertEquals(4.0, browserSlice.value());
    }

    @Test
    void differenceReportTreatsExpiredSupportAsCriticalEvenWhenUpToDate() throws SQLException {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        ReportService service = new ReportService(jdbc);

        ResultSet row = mock(ResultSet.class);
        when(row.getString("ProjectName")).thenReturn("Project X");
        when(row.getString("ProjectSAPID")).thenReturn("SAP-X");
        when(row.getString("SiteName")).thenReturn("Site Alpha");
        when(row.getString("VariantCode")).thenReturn("VAR-1");
        when(row.getString("software_name")).thenReturn("Core");
        when(row.getString("current_release")).thenReturn("2024");
        when(row.getString("current_revision")).thenReturn("1");
        when(row.getString("target_release")).thenReturn("2024");
        when(row.getString("target_revision")).thenReturn("1");
        when(row.getString("install_status")).thenReturn("Installed");
        LocalDate now = LocalDate.now();
        when(row.getObject("support_end")).thenReturn(Date.valueOf(now.minusDays(1)));

        when(jdbc.query(anyString(), anyMap(), any(RowMapper.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            RowMapper<Map<String, Object>> mapper = (RowMapper<Map<String, Object>>) invocation.getArgument(2);
            List<Map<String, Object>> mappedRows = new ArrayList<>();
            try {
                mappedRows.add(mapper.mapRow(row, 0));
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return mappedRows;
        });

        ReportFilter filter = new ReportFilter(ReportType.DIFFERENCE, null, null, null, null, null, null);
        ReportResponse response = service.getReport(filter);

        Map<String, Object> firstRow = response.table().rows().get(0);
        assertEquals("Critical", firstRow.get("severity"));
        assertEquals("Up to date", firstRow.get("compliance"));
    }
}
