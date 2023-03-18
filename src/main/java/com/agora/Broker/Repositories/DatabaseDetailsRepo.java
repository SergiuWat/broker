package com.agora.Broker.Repositories;

import com.agora.Broker.Entity.DatabaseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDetailsRepo extends JpaRepository<DatabaseDetails, Long> {
}
