package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.repository;

import com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByTeamLeadId(Long teamLeadId);

    List<Team> findByProjectName(String projectName);

    Optional<Team> findByProjectNameAndMemberId(String projectName, Long memberId); // Для перевірки учасників
}
