package com.agora.Broker.controller;

import com.agora.Broker.utils.Utils;
import com.agora.Broker.Entity.DatabaseDetails;
import com.agora.Broker.Service.DatabaseDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private DatabaseDetailsService databaseDetailsService;

    @GetMapping("/search")
    public ResponseEntity<String> searchData(@RequestParam String q){
        if(q.length()==0)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<DatabaseDetails> allData = databaseDetailsService.getAll();
        Map<String,ArrayList<Map<String, String>>> myMap = new HashMap<>();

        for(DatabaseDetails db : allData){
            String tableName = db.getTableName();
            String url = db.getUrl();
            String size = db.getDataSize();
            if(!myMap.containsKey(url)){
                ArrayList<Map<String, String>> tableNames = new ArrayList<>();
                tableNames.add(Map.of(tableName, size));
                myMap.put(url,tableNames);
            } else {
                ArrayList<Map<String, String>> tableNameData = myMap.get(url);
                tableNameData.add(Map.of(tableName, size));
                myMap.put(url, tableNameData);
            }
        }

        HashMap <String,String> keywordsMap = new HashMap<String,String>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.nullNode();
        String responseStr = "";
        HttpClient client = HttpClient.newBuilder().build(); // Create the HTTPClient
        try {
            for(Map.Entry<String, ArrayList<Map<String, String>>> entry : myMap.entrySet()){
                if (entry.getKey().equals("http://localhost:8088") || entry.getKey().equals("http://localhost:8086"))
                {
                    continue;
                }
                String url = entry.getKey() + "/get_meta";
                HttpRequest request = HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(5)).uri(URI.create(url)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                else {
                    String body = response.body();
                    if (entry.getKey().equals("http://localhost:8084"))
                    {
                        body = response.body().replace("[", "\"").replace("]", "\"");
                    }
                    if (entry.getKey().equals("http://localhost:8082"))
                    {
                        body = response.body().replaceAll("schema", "@schema");
                    }
                    if (entry.getKey().equals("http://localhost:8087"))
                    {
                        body = response.body().replace("\r", "").replace("\n", "");
                    }
                    jsonNode = objectMapper.readTree(body);
                    Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                    Map.Entry<String, JsonNode> databaseName = fields.next();
                    JsonNode val = databaseName.getValue();
                    String dbNm = val.get("@schema").toString();
                    String dbName = Utils.removeFirstandLast(dbNm);
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String fieldValue = field.getValue().asText().toLowerCase();
                        String fieldKey = field.getKey().toLowerCase();
                        int index = 0;
                        for (int i = 0; i < entry.getValue().size(); i++)
                        {
                            if (entry.getValue().get(i).containsKey(fieldKey))
                            {
                                index = i;
                                break;
                            }
                        }


                        String fullFieldKey = (new StringBuilder()).append(dbName).append("/").append(fieldKey).toString();
                        if(fieldKey.contains(q.toLowerCase())){
                           responseStr = "{ \"@context\": { \"@schema\":"+"\""+fullFieldKey+"\""+" }, \"size\": " + entry.getValue().get(index).get(fieldKey) + " KB}";
                            return new ResponseEntity<>(responseStr,HttpStatus.OK);
                        } else if (fieldValue.contains(q.toLowerCase())){
                            responseStr = "{ \"@context\": { \"@schema\":"+"\""+fullFieldKey+"\""+" }, \"size\": " + entry.getValue().get(index).get(fieldKey) + " KB}";
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
