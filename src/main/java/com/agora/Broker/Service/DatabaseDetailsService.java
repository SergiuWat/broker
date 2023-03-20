package com.agora.Broker.Service;

import com.agora.Broker.Entity.DatabaseDetails;
import com.agora.Broker.Repositories.DatabaseDetailsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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

    public List<DatabaseDetails> query(String dataBaseName, String tableName) {
        DatabaseDetails databaseDetails = new DatabaseDetails();
        databaseDetails.setDataBaseName(dataBaseName);
        databaseDetails.setTableName(tableName);
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withIgnoreCase()
            .withMatcher("dataBaseName", match -> match.contains())
            .withMatcher("tableName", match -> match.contains());
        Example<DatabaseDetails> exampleQuery = Example.of(databaseDetails, matcher);

       return databaseDetailsRepo.findAll(exampleQuery);
    }
}
