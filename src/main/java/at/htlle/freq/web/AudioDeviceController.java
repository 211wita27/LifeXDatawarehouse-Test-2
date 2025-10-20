package at.htlle.freq.web;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/** Vollständiger CRUD-Controller für AudioDevices (Audio-Peripherie) */
@RestController
@RequestMapping("/audio")
public class AudioDeviceController {

    private final NamedParameterJdbcTemplate jdbc;

    public AudioDeviceController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ----------------------------
    // READ: Alle oder nach Client filtern
    // ----------------------------
    @GetMapping
    public List<Map<String, Object>> findByClient(@RequestParam(required = false) String clientId) {
        if (clientId != null) {
            return jdbc.queryForList("""
                SELECT AudioDeviceID, ClientID, AudioDeviceBrand, DeviceSerialNr, 
                       AudioDeviceFirmware, DeviceType
                FROM AudioDevice
                WHERE ClientID = :cid
                """, new MapSqlParameterSource("cid", clientId));
        }

        return jdbc.queryForList("""
            SELECT AudioDeviceID, ClientID, AudioDeviceBrand, DeviceSerialNr, 
                   AudioDeviceFirmware, DeviceType
            FROM AudioDevice
            """, new HashMap<>());
    }

    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable String id) {
        var rows = jdbc.queryForList("""
            SELECT AudioDeviceID, ClientID, AudioDeviceBrand, DeviceSerialNr, 
                   AudioDeviceFirmware, DeviceType
            FROM AudioDevice
            WHERE AudioDeviceID = :id
            """, new MapSqlParameterSource("id", id));

        if (rows.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AudioDevice not found");
        return rows.get(0);
    }

    // ----------------------------
    // CREATE
    // ----------------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody Map<String, Object> body) {
        if (body.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");

        String sql = """
            INSERT INTO AudioDevice 
            (ClientID, AudioDeviceBrand, DeviceSerialNr, AudioDeviceFirmware, DeviceType)
            VALUES (:clientID, :audioDeviceBrand, :deviceSerialNr, :audioDeviceFirmware, :deviceType)
            """;

        jdbc.update(sql, new MapSqlParameterSource(body));
    }

    // ----------------------------
    // UPDATE
    // ----------------------------
    @PutMapping("/{id}")
    public void update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        if (body.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty body");

        var setClauses = new ArrayList<String>();
        for (String key : body.keySet()) {
            setClauses.add(key + " = :" + key);
        }

        String sql = "UPDATE AudioDevice SET " + String.join(", ", setClauses) +
                " WHERE AudioDeviceID = :id";

        var params = new MapSqlParameterSource(body).addValue("id", id);
        int updated = jdbc.update(sql, params);

        if (updated == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no audio device updated");
    }

    // ----------------------------
    // DELETE
    // ----------------------------
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        int count = jdbc.update("DELETE FROM AudioDevice WHERE AudioDeviceID = :id",
                new MapSqlParameterSource("id", id));

        if (count == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no audio device deleted");
    }
}
