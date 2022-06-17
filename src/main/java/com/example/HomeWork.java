package com.example;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class HomeWork {

    private String id;
    private String ownerId;
    private String status;
    private String course;
    private String dueDate;
    private String details;

    @JsonCreator
    public HomeWork(@JsonProperty("id") String id,
                @JsonProperty("ownerId") String ownerId,
                @JsonProperty("status") String status,
                @JsonProperty("course") String course,
                @JsonProperty("dueDate") String dueDate,
                @JsonProperty("details") String details) {

        this.id = id;
        this.ownerId = ownerId;
        this.status = status;
        this.course = course;
        this.dueDate = dueDate;
        this.details = details;
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

    public String getCourse() {return this.course;}

    public String getDueDate() {return this.dueDate;}

    public String getDetails() {return this.details;}

    public String toString() {
        return "Chore id: " + id + " ownerId: " + ownerId + " status: " + status + " course: " + course + " dueDate: " + dueDate + " details: " + details;
    }
    
}
