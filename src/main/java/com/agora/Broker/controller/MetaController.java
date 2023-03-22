package com.agora.Broker.controller;

import com.agora.Broker.utils.Utils;
import com.agora.Broker.Entity.DatabaseDetails;
import com.agora.Broker.Service.DatabaseDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.parser.ParserException;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class MetaController {

    private final DatabaseDetailsService databaseDetailsService;


    @GetMapping("/get_all")
    public List<DatabaseDetails> getAll(){
        return databaseDetailsService.getAll();
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchData(@RequestParam String q){
        if(q.length()==0)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<DatabaseDetails> allData = databaseDetailsService.getAll();
        Map<String,ArrayList<String>> myMap = new HashMap<String,ArrayList<String>>();
        //we store all databases uri and their coresponding tables
        // in a hash map
        for(DatabaseDetails db : allData){
            String tableName = db.getTableName();
            String url = db.getUrl();
            if(!myMap.containsKey(url)){
                ArrayList<String> tableNames = new ArrayList<String>();
                tableNames.add(tableName);
                myMap.put(url,tableNames);
            } else {
                ArrayList<String> tableNameData = myMap.get(url);
                tableNameData.add(tableName);
                myMap.put(url, tableNameData);
            }
        }

        HashMap <String,String> keywordsMap = new HashMap<String,String>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.nullNode();
        Boolean checkTableField=false;
        String searchBar = "air quality";
        List<String> dataBases = new ArrayList<>();
        Map<String, String> contexts = new HashMap<>();
        dataBases.add("http://localhost:5000");
        String responseStr = "";
        HttpClient client = HttpClient.newBuilder().build(); // Create the HTTPClient
        try {
            for(int i =0; i<dataBases.size();i++){
                HttpRequest request = HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(5)).uri(URI.create(dataBases.get(i) + "/get_meta")).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                else {
                    jsonNode = objectMapper.readTree(response.body());
                    Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                    Map.Entry<String, JsonNode> databaseName = fields.next();
                    JsonNode val = databaseName.getValue();
                    String dbNm = val.get("@schema").toString();
                    String dbName = Utils.removeFirstandLast(dbNm);
                    System.out.println(dbName);
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String fieldValue = field.getValue().asText().toLowerCase();
                        String fieldKey = field.getKey().toLowerCase();
                        String dbLocation = dataBases.get(i);
                        System.out.println(fieldKey);
                        System.out.println(fieldValue);

//                        String fullFieldKey = dbName+"/"+fieldKey;
                        String fullFieldKey = (new StringBuilder()).append(dbName).append("/").append(fieldKey).toString();
                        if(fieldKey.contains(q)){
                           responseStr = "{ \"@context\":{\"@schema\":"+"\""+fullFieldKey+"\""+"}}";
                            return new ResponseEntity<>(responseStr,HttpStatus.OK);
                        } else if (fieldValue.contains(q)){
                            responseStr = "{ \"@context\":{\"@schema\":"+"\""+fullFieldKey+"\""+"}}";
                            return new ResponseEntity<>(responseStr,HttpStatus.OK);
                        }
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
