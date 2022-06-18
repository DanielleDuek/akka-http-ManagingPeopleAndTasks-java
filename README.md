# WELCOME !
--------------
- RESTful server for managing people and tasks, Based on Akka Http and SQLite Database.
- Created by Danielle Duek (318465333) and Shahaf (205411978).
- We built our Akka HTTP project with the help of the [**Akka tool**](https://akka.io/).
- We created our database with the help of the [**SQLite home page**](https://sqlite.org/index.html).



# Requirements
--------------
- JDK 11
- sbt 1.4.5 or higher
- SQLite



# Instructions
--------------
- First, you need to download SQLite and create a new Database.
  Then, you need to put the path that lead to the new Database you created (your db file) in your computer in line 70 (in the url variable) in `App.java` file.
- To run the server, press `sbt run` in the command prompt.
- To send an HTTP Request, you need to use the URL : `http://localhost:8080/{PATH}`.
- To terminate the server running, you need to press `return` in the command prompt. 



# Our Tables
--------------
Our tables create automatically when the server starting to run.

|Persons                       |  
|------------------------------|
|Id                            |
|Name                          |
|Email                         |
|Favorite Programming Language |

|Tasks                         |
|------------------------------|
|Id                            |
|OwnerId                       |
|Type                          |
|Status                        |

|Chore                         |      
|------------------------------|  
|TaskId                        | 
|Description                   |      
|Size                          |      
                                     
|HomeWork                      |
|------------------------------|
|TaskId                        |
|Course                        |
|DueDate                       |
|Details                       |



# Architecture
--------------
- In the `App.java` file we have 3 importent parts:
The first part, is connecting to the databse in the "connect" function.
The second part, is creating tables using executing SQL queries in "createTables" function.
In this part we also enforce the table requirements (assigning AUTO-INCREMENT ID for persons and tasks, for example)
The third part is the routing function called "Routes".
This function is assigns a path to each request and adjusts the breakpoint to the function that needs to be performed. The whole server is based on this route.

- The Class `Person.java`:
In this class we specify the properties of each person according the table requirments.
We added a `PersonDetails.java` class to return the preson details in a GET request, so the "activeTaskCount" property added to it.

- The Class `GenericTask.java`:
In this class we specify all the properties of the two tasks types together (Chore and HomeWork) and in addition the "type" property.
We need the GenericTask class to the json parsing part. When we get a task from the user, we parsing it as a generic task and then we checking the type and accordingly we build the appropriate object (`Chore.java` class or `HomeWork.java` class).



# The RESTful API system supports 
--------------
|Request                        |Routings                                 |
|-------------------------------|-----------------------------------------|
|POST a person                  |http://localhost:8080/people             |
|GET all people                 |http://localhost:8080/people             |
|GET a person                   |http://localhost:8080/people/{id}        |
|PATCH a person                 |http://localhost:8080/people/{id}        |
|DELETE a person                |http://localhost:8080/people/{id}        |
|GET all tasks of a person      |http://localhost:8080/people/{id}        |
|POST a task to a person        |http://localhost:8080/people/{id}        |
|GET a task                     |http://localhost:8080/tasks/{id}         |
|PATCH a task                   |http://localhost:8080/tasks/{id}         |
|DELETE a task                  |http://localhost:8080/tasks/{id}         |
|GET a task's status            |http://localhost:8080/tasks/{id}/status  |
|PUT new status to a task       |http://localhost:8080/tasks/{id}/status  |
|GET a task's owner id          |http://localhost:8080/tasks/{id}/owner   |
|PUT new owner id to a task     |http://localhost:8080/tasks/{id}/status  |



# GOOD LUCK !

