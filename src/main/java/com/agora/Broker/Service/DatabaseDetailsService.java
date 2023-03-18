package com.agora.Broker.Service;

import com.agora.Broker.Entity.DatabaseDetails;
import com.agora.Broker.Repositories.DatabaseDetailsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseDetailsService {
    private final DatabaseDetailsRepo databaseDetailsRepo;
    public DatabaseDetails saveDatabaseDetails(DatabaseDetails databaseDetails){
        return databaseDetailsRepo.save(databaseDetails);
    }

    public void deleteTable(Long id){
        databaseDetailsRepo.deleteById(id);
    }

    public List<DatabaseDetails> getAll(){
        return databaseDetailsRepo.findAll();
    }
}
