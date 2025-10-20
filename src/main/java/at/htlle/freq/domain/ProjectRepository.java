package at.htlle.freq.domain;

import java.util.*;

public interface ProjectRepository {
    Optional<Project> findById(UUID id);
    Optional<Project> findBySapId(String sapId);
    Project save(Project project);
    List<Project> findAll();
}
