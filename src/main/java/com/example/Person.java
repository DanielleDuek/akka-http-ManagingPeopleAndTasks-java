package com.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
    
    public String id;
    public String name;
    public String email;
    public String favoriteProgrammingLanguage;

    @JsonCreator
    public Person(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("email") String email, @JsonProperty("favoriteProgrammingLanguage") String favoriteProgrammingLanguage) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.favoriteProgrammingLanguage = favoriteProgrammingLanguage;
    }

    public String getId() {return this.id;}

    public String getName() {return this.name;}

    public String getEmail() {return this.email;}

    public String getFavoriteProgrammingLanguage() {return this.favoriteProgrammingLanguage;}

    public String toString() {
        return "Person - id:" + id + " name:" + name + " email:" + email + " favoriteProgrammingLanguage:" + favoriteProgrammingLanguage;
    }

}
