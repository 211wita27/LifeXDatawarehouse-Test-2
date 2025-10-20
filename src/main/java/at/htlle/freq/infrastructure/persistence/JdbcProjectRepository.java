package at.htlle.freq.infrastructure.persistence;

import at.htlle.freq.domain.Project;
import at.htlle.freq.domain.ProjectRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JdbcProjectRepository implements ProjectRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcProjectRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Project> mapper = (rs, n) -> new Project(
            rs.getObject("ProjectID", UUID.class),
            rs.getString("ProjectSAPID"),
            rs.getString("ProjectName"),
            rs.getObject("DeploymentVariantID", UUID.class),
            rs.getString("BundleType"),
            rs.getString("CreateDateTime"),
            rs.getBoolean("StillActive"),
            rs.getObject("AccountID", UUID.class),
            rs.getObject("AddressID", UUID.class)
    );

    @Override
    public Optional<Project> findById(UUID id) {
        String sql = """
            SELECT ProjectID, ProjectSAPID, ProjectName, DeploymentVariantID, BundleType,
                   CreateDateTime, StillActive, AccountID, AddressID
            FROM Project WHERE ProjectID = :id
            """;
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), mapper));
        } catch (Exception e) { return Optional.empty(); }
    }

    @Override
    public Optional<Project> findBySapId(String sapId) {
        String sql = """
            SELECT ProjectID, ProjectSAPID, ProjectName, DeploymentVariantID, BundleType,
                   CreateDateTime, StillActive, AccountID, AddressID
            FROM Project WHERE ProjectSAPID = :sap
            """;
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new MapSqlParameterSource("sap", sapId), mapper));
        } catch (Exception e) { return Optional.empty(); }
    }

    @Override
    public List<Project> findAll() {
        String sql = """
            SELECT ProjectID, ProjectSAPID, ProjectName, DeploymentVariantID, BundleType,
                   CreateDateTime, StillActive, AccountID, AddressID
            FROM Project
            """;
        return jdbc.query(sql, mapper);
    }

    @Override
    public Project save(Project p) {
        boolean isNew = p.getProjectID() == null;
        if (isNew) {
            String sql = """
                INSERT INTO Project (ProjectSAPID, ProjectName, DeploymentVariantID, BundleType,
                                     CreateDateTime, StillActive, AccountID, AddressID)
                VALUES (:sap, :name, :dv, :bundle, :created, :active, :account, :address)
                RETURNING ProjectID
                """;
            var params = new MapSqlParameterSource()
                    .addValue("sap",     p.getProjectSAPID())
                    .addValue("name",    p.getProjectName())
                    .addValue("dv",      p.getDeploymentVariantID())
                    .addValue("bundle",  p.getBundleType())
                    .addValue("created", p.getCreateDateTime())
                    .addValue("active",  p.isStillActive())
                    .addValue("account", p.getAccountID())
                    .addValue("address", p.getAddressID());
            UUID id = jdbc.queryForObject(sql, params, UUID.class);
            p.setProjectID(id);
        } else {
            String sql = """
                UPDATE Project SET
                    ProjectSAPID = :sap,
                    ProjectName = :name,
                    DeploymentVariantID = :dv,
                    BundleType = :bundle,
                    CreateDateTime = :created,
                    StillActive = :active,
                    AccountID = :account,
                    AddressID = :address
                WHERE ProjectID = :id
                """;
            var params = new MapSqlParameterSource()
                    .addValue("id",      p.getProjectID())
                    .addValue("sap",     p.getProjectSAPID())
                    .addValue("name",    p.getProjectName())
                    .addValue("dv",      p.getDeploymentVariantID())
                    .addValue("bundle",  p.getBundleType())
                    .addValue("created", p.getCreateDateTime())
                    .addValue("active",  p.isStillActive())
                    .addValue("account", p.getAccountID())
                    .addValue("address", p.getAddressID());
            jdbc.update(sql, params);
        }
        return p;
    }
}
