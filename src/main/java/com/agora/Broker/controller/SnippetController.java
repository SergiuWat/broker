package com.agora.Broker.controller;

import com.agora.Broker.Entity.DatabaseDetails;
import com.agora.Broker.Service.DatabaseDetailsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class SnippetController {
    @Autowired
    private DatabaseDetailsService databaseDetailsService;

    @GetMapping("/get_snippet")
    public ResponseEntity<JsonNode> get_snippet(@RequestParam(name="databaseName") String dataBaseName, @RequestParam(name="databaseTable") String tableName) throws java.io.IOException, java.lang.InterruptedException, java.net.http.HttpConnectTimeoutException{
        ObjectMapper objectMapper = new ObjectMapper();

        List<DatabaseDetails> allData = databaseDetailsService.getAll();
        String url = "";
        String tbName = "";
        String dataType = "";
        for(DatabaseDetails db : allData){
            tbName = db.getTableName();
            dataType = db.getDataType();
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
            HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url + "/get_data?data_type=" + dataType + "&snippet=true")).build();
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
    public void get_dataset(@RequestParam(name="databaseName") String dataBaseName, @RequestParam(name="databaseTable") String tableName, @RequestParam String format, HttpServletResponse responseAll) throws java.io.IOException, java.lang.InterruptedException, java.net.http.HttpConnectTimeoutException{
        ObjectMapper objectMapper = new ObjectMapper();

        List<DatabaseDetails> allData = databaseDetailsService.getAll();
        String url = "";
        String dataType = "";
        String tbName = "";
        for(DatabaseDetails db : allData){
            tbName = db.getTableName();
            String dbName = db.getDataBaseName();
            dataType = db.getDataType();
            if (tbName.toLowerCase().contains(tableName.toLowerCase()) && dbName.toLowerCase().contains(dataBaseName.toLowerCase()))
            {
                url = db.getUrl();
                break;
            }
        }
        String port = url.split(":")[2];

        HttpClient client = HttpClient.newBuilder().build();
        JsonNode jsonNode = objectMapper.nullNode();
        try {
            if (format.matches("json-ld"))
            {
                HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url + "/get_data?data_type=" + dataType + "&snippet=false")).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    jsonNode = objectMapper.readTree(response.body());
                    responseAll.setContentType("application/json; charset=utf-8");
                    responseAll.getWriter().print(jsonNode);
                }
                else if (response.statusCode() == 500)
                {
                    responseAll.sendError(500);
                }
            }
            else if (format.matches("csv") || format.matches("xml") || format.matches("json")){
                String urlEncoder = URLEncoder.encode("&snippet", "utf-8");
                String requestURL = "http://localhost:8089/get_data?type=" + format + "&apiUrl=http://192.168.1.2:" + port + "/get_data?data_type=" + dataType + urlEncoder + "=false";
                HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(requestURL)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200)
                {
                    switch (format){
                        case "csv":
                            responseAll.setContentType("text/csv; charset=utf-8");
                            responseAll.getWriter().print(response.body());
                            break;
                        case "xml":
                            responseAll.setContentType("application/xml; charset=utf-8");
                            responseAll.getWriter().print(response.body());
                            break;
                        case "json":
                            jsonNode = objectMapper.readTree(response.body());
                            responseAll.setContentType("application/json; charset=utf-8");
                            responseAll.getWriter().print(jsonNode);
                            break;
                    }
                }
                else
                {
                    responseAll.sendError(500);
                }
            }
            else {
                responseAll.sendError(400, "Format incorrect!");
            }
        }
        catch (Exception e) {
            throw e;
        }
    }
}
