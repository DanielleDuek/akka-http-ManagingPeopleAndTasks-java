package com.example;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericTask {
    
    private final String id;
    private final String ownerId;
    private final String status;
    private final String type;
    private final String description;
    private final String size;
    private final String course;
    private final String dueDate;
    private final String details;

    @JsonCreator
    public GenericTask(@JsonProperty("id") String id,
                        @JsonProperty("ownerId") String ownerId,
                        @JsonProperty("status") String status,
                        @JsonProperty("type") String type,
                        @JsonProperty("description") String description,
                        @JsonProperty("size") String size,
                        @JsonProperty("course") String course,
                        @JsonProperty("dueDate") String dueDate,
                        @JsonProperty("details") String details) {
                            
        this.id = id;
        this.ownerId = ownerId;
        this.status = status;
        this.type = type;
        this.description = description;
        this.size = size;
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

    public String getDescription() {return this.description;}

    public String getSize() {return this.size;}

    public String getType() {return this.type;}

    public String getCourse() {return this.course;}

    public String getDueDate() {return this.dueDate;}

    public String getDetails() {return this.details;}

}
