package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Telefon-Integrationen zu einem Client. */
@RestController
public class PhoneController {

    private final JdbcTemplate jdbc;

    public PhoneController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/phones")
    public List<Map<String,Object>> findByClient(@RequestParam int clientId) {
        return jdbc.queryForList("""
                SELECT PhoneIntegrationID, PhoneType, PhoneBrand, PhoneFirmware, ClientID
                FROM PhoneIntegration
                WHERE ClientID = ?
                """, clientId);
    }
}