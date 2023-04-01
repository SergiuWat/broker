package com.agora.Broker.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DatabaseDetails")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Long id;
    private String dataBaseName;
    private String url;
    private String dataSize;

    private String tableName;

    private String dataType;
}
