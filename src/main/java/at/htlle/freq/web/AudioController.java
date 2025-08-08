package at.htlle.freq.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Audio-Devices zu einem Client. */
@RestController
public class AudioController {

    private final JdbcTemplate jdbc;

    public AudioController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/audio")
    public List<Map<String,Object>> findByClient(@RequestParam int clientId) {
        return jdbc.queryForList("""
                SELECT AudioDeviceID, AudioDeviceBrand, AudioDeviceFirmware, Direction, ClientID
                FROM AudioDevice
                WHERE ClientID = ?
                """, clientId);
    }
}