package com.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDetails {
    
    private String id;
    private String name;
    private String email;
    private String favoriteProgrammingLanguage;
    private int activeTaskCount;


    @JsonCreator
    public PersonDetails(@JsonProperty("id") String id, 
                        @JsonProperty("name") String name, 
                        @JsonProperty("email") String email, 
                        @JsonProperty("favoriteProgrammingLanguage") String favoriteProgrammingLanguage,
                        @JsonProperty("activeTaskCount") int activeTaskCount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.favoriteProgrammingLanguage = favoriteProgrammingLanguage;
        this.activeTaskCount = activeTaskCount;
    }

    public String getId() {return this.id;}

    public String getName() {return this.name;}

    public String getEmail() {return this.email;}

    public String getFavoriteProgrammingLanguage() {return this.favoriteProgrammingLanguage;}

    public int getActiveTaskCount() {return this.activeTaskCount;}

    public String toString() {
        return "Person - id:" + id + " name:" + name + " email:" + email + " favoriteProgrammingLanguage:" + favoriteProgrammingLanguage + " activeTaskCount: " + activeTaskCount;
    }
}
