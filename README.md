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
|People                        |
|------------------------------|
|Id                            |
|Name                          |
|Email                         |
|Favorite Programming Language |




# The RESTful API system supports 
--------------
|Request                        |Routings                                     |
|-------------------------------|---------------------------------------------|
|POST a person                  |http://localhost:8080/api/people             |
|GET all people                 |http://localhost:8080/api/people             |
|GET a person                   |http://localhost:8080/api/people/{id}        |
|PATCH a person                 |http://localhost:8080/api/people/{id}        |
|DELETE a person                |http://localhost:8080/api/people/{id}        |
|GET all tasks of a person      |http://localhost:8080/api/people/{id}        |
|POST a task to a person        |http://localhost:8080/api/people/{id}        |
|GET a task                     |http://localhost:8080/api/tasks/{id}         |
|PATCH a task                   |http://localhost:8080/api/tasks/{id}         |
|DELETE a task                  |http://localhost:8080/api/tasks/{id}         |
|GET a task's status            |http://localhost:8080/api/tasks/{id}/status  |
|PUT new status to a task       |http://localhost:8080/api/tasks/{id}/status  |
|GET a task's owner id          |http://localhost:8080/api/tasks/{id}/owner   |
|PUT new owner id to a task     |http://localhost:8080/api/tasks/{id}/status  |





# GOOD LUCK !

