package com.agora.Broker.controller;

import com.agora.Broker.Entity.DatabaseDetails;
import com.agora.Broker.Service.DatabaseDetailsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agora.Broker.controller.MetaController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class SnippetController {
    private final DatabaseDetailsService databaseDetailsService;

    @GetMapping("/get_snippet")
    public ResponseEntity<JsonNode> get_snippet_true(@RequestParam(name="databaseName") String dataBaseName, @RequestParam(name="databaseTable") String tableName) throws java.io.IOException, java.lang.InterruptedException, java.net.http.HttpConnectTimeoutException{
        ObjectMapper objectMapper = new ObjectMapper();

        List<DatabaseDetails> allData = databaseDetailsService.getAll();
        String url = "";
        for(DatabaseDetails db : allData){
            String tbName = db.getTableName();
            String dbName = db.getDataBaseName();
            if (tbName.toLowerCase().contains(tableName.toLowerCase()) && dbName.toLowerCase().contains(dataBaseName.toLowerCase()))
            {
                url = db.getUrl();
                break;
            }
        }

        HttpClient client = HttpClient.newBuilder().build();
        JsonNode jsonNode = objectMapper.nullNode();
        try {
            HttpRequest request = HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(5)).uri(URI.create(url + "/get_data?data_type=" + tableName + "&snippet=true")).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
            {
                jsonNode = objectMapper.readTree(response.body());
            }
            else if (response.statusCode() == 500)
            {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objectMapper.nullNode());
            }
        }
        catch (Exception e) {
            throw e;
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonNode);
    }

    @GetMapping("/get_dataset")
    public ResponseEntity<JsonNode> get_snippet_false(@RequestParam(name="databaseName") String dataBaseName, @RequestParam(name="databaseTable") String tableName, @RequestParam String format) throws java.io.IOException, java.lang.InterruptedException, java.net.http.HttpConnectTimeoutException{
        ObjectMapper objectMapper = new ObjectMapper();

        List<DatabaseDetails> allData = databaseDetailsService.getAll();
        String url = "";
        for(DatabaseDetails db : allData){
            String tbName = db.getTableName();
            String dbName = db.getDataBaseName();
            if (tbName.toLowerCase().contains(tableName.toLowerCase()) && dbName.toLowerCase().contains(dataBaseName.toLowerCase()))
            {
                url = db.getUrl();
                break;
            }
        }


        HttpClient client = HttpClient.newBuilder().build();
        JsonNode jsonNode = objectMapper.nullNode();
        try {
            HttpRequest request = HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(10)).uri(URI.create(url + "/get_data?data_type=" + tableName + "&snippet=false")).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
            {
                if (format.matches("json-ld"))
                {
                    jsonNode = objectMapper.readTree(response.body());
                }
                else {
                    //@TODO Apel microserviciu de conversie
                    jsonNode = objectMapper.nullNode();
                }
            }
            else if (response.statusCode() == 500)
            {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objectMapper.nullNode());
            }
        }
        catch (Exception e) {
            throw e;
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonNode);
    }
}
