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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class SnippetController {
    private final DatabaseDetailsService databaseDetailsService;

    @GetMapping("/get_snippet/true")
    public ResponseEntity<JsonNode> get_snippet_true(@RequestBody JsonNode body) throws java.io.IOException, java.lang.InterruptedException, java.net.http.HttpConnectTimeoutException{
        ObjectMapper objectMapper = new ObjectMapper();
        Iterator<Map.Entry<String, JsonNode>> fetch = body.fields();
        String dataBaseName = "";
        String tableName = "";
        while (fetch.hasNext())
        {
            Map.Entry<String, JsonNode> dataFetched = fetch.next();
            String key = dataFetched.getKey();
            String value = dataFetched.getValue().asText();
            if (key.matches("databaseName"))
            {
                dataBaseName = value;
            }
            if (key.matches("databaseTable"))
            {
                tableName = value;
            }

        }
        HttpClient client = HttpClient.newBuilder().build();
        List<DatabaseDetails> databaseDetails = databaseDetailsService.query(dataBaseName, tableName);
        JsonNode jsonNode = objectMapper.nullNode();
        for (int i = 0; i < databaseDetails.size(); i++) {
            String url = databaseDetails.get(i).getUrl();
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
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonNode);
    }

    @GetMapping("/get_snippet/false")
    public ResponseEntity<JsonNode> get_snippet_false(@RequestBody JsonNode body) throws java.io.IOException, java.lang.InterruptedException, java.net.http.HttpConnectTimeoutException{
        ObjectMapper objectMapper = new ObjectMapper();
        Iterator<Map.Entry<String, JsonNode>> fetch = body.fields();
        String dataBaseName = "";
        String tableName = "";
        String format = "";
        while (fetch.hasNext())
        {
            Map.Entry<String, JsonNode> dataFetched = fetch.next();
            String key = dataFetched.getKey();
            String value = dataFetched.getValue().asText();
            if (key.matches("databaseName"))
            {
                dataBaseName = value;
            }
            if (key.matches("databaseTable"))
            {
                tableName = value;
            }
            if (key.matches("format"))
            {
                format = value.toLowerCase();
            }
        }

        HttpClient client = HttpClient.newBuilder().build();
        List<DatabaseDetails> databaseDetails = databaseDetailsService.query(dataBaseName, tableName);
        JsonNode jsonNode = objectMapper.nullNode();
        for (int i = 0; i < databaseDetails.size(); i++) {
            String url = databaseDetails.get(i).getUrl();
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
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonNode);
    }
}
