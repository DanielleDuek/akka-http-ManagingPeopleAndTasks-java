package com.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Chore {

    private String id;
    private String ownerId;
    private String status;
    private String description;
    private String size;

    @JsonCreator
    public Chore(@JsonProperty("id") String id,
                @JsonProperty("ownerId") String ownerId,
                @JsonProperty("status") String status,
                @JsonProperty("description") String description,
                @JsonProperty("size") String size) {

        this.id = id;
        this.ownerId = ownerId;
        this.status = status;
        this.description = description;
        this.size = size;
    }


    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getStatus() {
        return status;
    }

    public void setOwner(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {return this.description;}

    public String getSize() {return this.size;}

    public String toString() {
        return "Chore id: " + id + " ownerId: " + ownerId + " status: " + status + " description: " + description + " size: " + size;
    }
    
}
