package com.example;

import akka.Done;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.ActorSystem;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.PathMatchers;


public class App extends AllDirectives{

    private static Connection conn = null;  
    private static Statement stmt = null;

    public static void main(String[] args) throws Exception {
        // boot up server using the route as defined below
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes");
    
        final Http http = Http.get(system);
    
        //In order to access all directives we need an instance where the routes are define.
        App app = new App();
    
        final CompletionStage<ServerBinding> binding =
          http.newServerAt("localhost", 8080)
              .bind(app.Routes());
              
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        connect();
        System.in.read(); // let it run until user presses return
    
        binding
            .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
            .thenAccept(unbound -> {
                if (conn != null) {  
                    try {
                        if (stmt == null) {
                            stmt = conn.createStatement();
                        }
                        stmt.executeUpdate("DROP TABLE Persons");
                        stmt.executeUpdate("DROP TABLE Tasks");
                        stmt.executeUpdate("DROP TABLE Chore");
                        stmt.executeUpdate("DROP TABLE HomeWork");
                        stmt.close();
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }  
                } 
                system.terminate();}); // and shutdown when done
    }

    public static void connect() throws ClassNotFoundException {  
        try {  
            // YOU NEED TO PUT THE DataBase PATH HERE !
            String url = "jdbc:sqlite:C:/sqlite/AppDB.db";  
            
            // create a connection to the database  
            conn = DriverManager.getConnection(url); 
            System.out.println("Connection to SQLite has been established.");  
            
            createTables();

        } catch (SQLException e) {  
            System.out.println(e.getMessage()); 
        } 
    } 

    public static void createTables() throws ClassNotFoundException {
        try {
            stmt = conn.createStatement();
            String createPeopleTableQuery = 
                "CREATE TABLE Persons " + 
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "favoriteProgrammingLanguage TEXT NOT NULL)";
            stmt.executeUpdate(createPeopleTableQuery);
            String createTasksTableQuery = 
                "CREATE TABLE Tasks " + 
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ownerId TEXT NOT NULL, " + 
                "type TEXT CHECK(type IN ('Chore', 'HomeWork')) NOT NULL," + 
                "status TEXT DEFAULT 'Active' CHECK(status IN ('Done', 'Active')) NOT NULL," + 
                "FOREIGN KEY(ownerId) REFERENCES Persons(id))";
            stmt.executeUpdate(createTasksTableQuery);
            String createChoreTable = 
                "CREATE TABLE Chore " +
                "(taskId INTEGER PRIMARY KEY, " +
                "description TEXT NOT NULL, " +
                "size TEXT CHECK(size IN ('Small', 'Medium', 'Large')), " +
                "FOREIGN KEY(taskId) REFERENCES Tasks(id))";
            stmt.executeUpdate(createChoreTable);
            String createHomeworkTable = 
                "CREATE TABLE HomeWork " +
                "(taskId INTEGER PRIMARY KEY, " +
                "course TEXT NOT NULL, " +
                "dueDate TEXT NOT NULL, " +
                "details TEXT NOT NULL, " +
                "FOREIGN KEY(taskId) REFERENCES Tasks(id))";
            stmt.executeUpdate(createHomeworkTable);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Route Routes() {
        final ExceptionHandler exceptionHandler = ExceptionHandler.newBuilder()
            .match(RuntimeException.class, x ->
                complete(StatusCodes.INTERNAL_SERVER_ERROR, x.getMessage()))
            .build();
        return concat(
            pathPrefix("people", () ->
                    concat(
                        pathEnd(() -> 
                            concat(
                                post(() -> 
                                    entity(
                                        Jackson.unmarshaller(Person.class), 
                                        person ->
                                            handleExceptions(exceptionHandler, () -> onComplete(addPerson(person), done -> complete("Person created successfully")))
                                    )
                                ),
                                get(() -> handleExceptions(exceptionHandler, () -> onComplete(getPeopleList(), done ->  complete(StatusCodes.OK, done, Jackson.marshaller())))
                                )
                            )
                        ),
                        path(PathMatchers.segment(), (String id) -> 
                            concat(
                                get(() -> 
                                    handleExceptions(exceptionHandler, () -> onSuccess(getPersonById(id), done -> complete(StatusCodes.OK, done, Jackson.marshaller())))
                                ),
                                patch(() -> 
                                    entity(
                                        Jackson.unmarshaller(Person.class),
                                        person -> handleExceptions(exceptionHandler, () -> onSuccess(updatePersonById(person, id), done -> complete(StatusCodes.OK, done, Jackson.marshaller())))

                                    )
                                ),
                                delete(() -> 
                                    handleExceptions(exceptionHandler, () -> onSuccess(deletePersonById(id), deleted -> complete("Person removed successfully")))
                                )
                            )
                        ),
                        path(PathMatchers.segment().slash("tasks"), (String id) ->
                            concat(
                                post(() -> 
                                    entity(
                                        Jackson.unmarshaller(GenericTask.class), 
                                        task -> handleExceptions(exceptionHandler, () -> onSuccess(addTask(id, task), done -> complete("Task created and assigned successfully")))
                                    )
                                ), 
                                get(() ->
                                    parameterOptional("status", status ->
                                        handleExceptions(exceptionHandler, () -> onSuccess(getTasksByPersonId(id, status), done -> complete(StatusCodes.OK, done, Jackson.marshaller())))
                                    )
                                )
                            )
                        )
                    )
            ),
            pathPrefix("tasks", () -> 
                concat(
                    path(PathMatchers.segment(), (String id) ->
                        concat(
                            delete(() ->                                 
                                handleExceptions(exceptionHandler, () -> onSuccess(deleteTaskById(id), done -> complete("Task removed successfully")))  
                            ),
                            get(() ->
                                handleExceptions(exceptionHandler, () -> onSuccess(getTaskById(id), done -> complete(StatusCodes.OK, done, Jackson.marshaller())))
                            ),
                            patch(() -> 
                                entity(
                                    Jackson.unmarshaller(GenericTask.class), 
                                    task -> handleExceptions(exceptionHandler, () -> onSuccess(updateTaskById(task, id), done -> complete(StatusCodes.OK, done, Jackson.marshaller())))
                                )
                            )
                        )
                    ),
                    path(PathMatchers.segment().slash("status"), (String id) -> 
                        concat(
                            get(() -> 
                                handleExceptions(exceptionHandler, () -> onSuccess(getStatusTaskById(id), done -> complete(StatusCodes.OK, done, Jackson.marshaller())))  
                            ),
                            put(() -> 
                                entity(
                                    Jackson.unmarshaller(String.class),
                                    status -> 
                                        handleExceptions(exceptionHandler, () -> onSuccess(updateTaskStatusById(status, id), done -> complete("task's status updated successfully")))  
                                )  
                            )
                        )
                    ),
                    path(PathMatchers.segment().slash("owner"), (String id) -> 
                        concat(
                            get(() -> 
                                handleExceptions(exceptionHandler, () -> onSuccess(getOwnerIdById(id), done -> complete(StatusCodes.OK, done, Jackson.marshaller())))     
                            ),
                            put(() -> 
                                entity(
                                    Jackson.unmarshaller(String.class),
                                    ownerId -> 
                                        handleExceptions(exceptionHandler, () -> onSuccess(updateOwnerIdById(ownerId, id), done -> complete("task owner updated successfully")))  
                                )  
                            )
                        )
                    )
                )
            )
        );
    }



    private CompletionStage<List<GenericTask>> getTasksByPersonId(String id, Optional<String> status) {
        List<GenericTask> genericTasks = new LinkedList<>();
        try {
            stmt = conn.createStatement();  
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Persons WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A person with the id '" + id + "' does not exist");
            }
            if (status.isPresent()) { // If status is provided
                if (status.get().equals("Done")) {
                    String getTaskChoreByPerson = "SELECT Tasks.id , Tasks.type, Chore.description, Chore.size From Tasks JOIN Chore ON Tasks.id = Chore.taskId WHERE ownerId = " + Integer.valueOf(id) + " AND status = 'Done'";
                    String getTaskHomeWorkByPerson = "SELECT Tasks.id , Tasks.type, HomeWork.course, HomeWork.dueDate, HomeWork.details From Tasks JOIN HomeWork ON Tasks.id = HomeWork.taskId WHERE ownerId = " + Integer.valueOf(id) + " AND status = 'Done'";
                    ResultSet choreTasks = stmt.executeQuery(getTaskChoreByPerson);
                    while (choreTasks.next()) {
                        String taskId = String.valueOf(choreTasks.getInt("id"));
                        String type = choreTasks.getString("type");
                        String description = choreTasks.getString("description");
                        String size = choreTasks.getString("size");
                        genericTasks.add(new GenericTask(taskId, id, status.get(), type, description, size, null, null, null));
                    }
                    ResultSet homeWorkTasks = stmt.executeQuery(getTaskHomeWorkByPerson);
                    while (homeWorkTasks.next()) {
                        String taskId = String.valueOf(homeWorkTasks.getInt("id"));
                        String type = homeWorkTasks.getString("type");
                        String course = homeWorkTasks.getString("course");
                        String dueDate = homeWorkTasks.getString("dueDate");
                        String details = homeWorkTasks.getString("details");
                        genericTasks.add(new GenericTask(taskId, id, status.get(), type, null, null, course, dueDate, details));
                    }
                } else {
                    if (status.get().equals("Active")) {    
                        String getTaskChoreByPerson = "SELECT Tasks.id , Tasks.type, Chore.description, Chore.size From Tasks JOIN Chore ON Tasks.id = Chore.taskId WHERE ownerId = " + Integer.valueOf(id) + " AND status = 'Active'";
                        String getTaskHomeWorkByPerson = "SELECT Tasks.id , Tasks.type, HomeWork.course, HomeWork.dueDate, HomeWork.details From Tasks JOIN HomeWork ON Tasks.id = HomeWork.taskId WHERE ownerId = " + Integer.valueOf(id) + " AND status = 'Active'";
                        ResultSet choreTasks = stmt.executeQuery(getTaskChoreByPerson);
                        while (choreTasks.next()) {
                            String taskId = String.valueOf(choreTasks.getInt("id"));
                            String type = choreTasks.getString("type");
                            String description = choreTasks.getString("description");
                            String size = choreTasks.getString("size");
                            genericTasks.add(new GenericTask(taskId, id, status.get(), type, description, size, null, null, null));
                        }
                        ResultSet homeWorkTasks = stmt.executeQuery(getTaskHomeWorkByPerson);
                        while (homeWorkTasks.next()) {
                            String taskId = String.valueOf(homeWorkTasks.getInt("id"));
                            String type = homeWorkTasks.getString("type");
                            String course = homeWorkTasks.getString("course");
                            String dueDate = homeWorkTasks.getString("dueDate");
                            String details = homeWorkTasks.getString("details");
                            genericTasks.add(new GenericTask(taskId, id, status.get(), type, null, null, course, dueDate, details));
                        }
                    }
                }
            } else { // If status is not provided
                String getTaskChoreByPerson = "SELECT Tasks.id, Tasks.type, Tasks.status, Chore.description, Chore.size From Tasks JOIN Chore ON Tasks.id = Chore.taskId WHERE ownerId = " + Integer.valueOf(id);
                String getTaskHomeWorkByPerson = "SELECT Tasks.id , Tasks.type, Tasks.status, HomeWork.course, HomeWork.dueDate, HomeWork.details From Tasks JOIN HomeWork ON Tasks.id = HomeWork.taskId WHERE ownerId = " + Integer.valueOf(id);
                ResultSet choreTasks = stmt.executeQuery(getTaskChoreByPerson);
                while (choreTasks.next()) {
                    String taskId = String.valueOf(choreTasks.getInt("id"));
                    String type = choreTasks.getString("type");
                    String taskStatus = choreTasks.getString("status");
                    String description = choreTasks.getString("description");
                    String size = choreTasks.getString("size");
                    genericTasks.add(new GenericTask(taskId, id, taskStatus, type, description, size, null, null, null));
                }
                ResultSet homeWorkTasks = stmt.executeQuery(getTaskHomeWorkByPerson);
                while (homeWorkTasks.next()) {
                    String taskId = String.valueOf(homeWorkTasks.getInt("id"));
                    String type = homeWorkTasks.getString("type");
                    String taskStatus = homeWorkTasks.getString("status");
                    String course = homeWorkTasks.getString("course");
                    String dueDate = homeWorkTasks.getString("dueDate");
                    String details = homeWorkTasks.getString("details");
                    genericTasks.add(new GenericTask(taskId, id, taskStatus, type, null, null, course, dueDate, details));
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(genericTasks);
}


    private CompletionStage<GenericTask> updateTaskById(GenericTask task, String id) {
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Tasks WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A task with the id '" + id + "' does not exist");
            }
            String type = task.getType();
            if (type == null) {
                stmt.close();
                throw new RuntimeException("Unable to update task without type");
            } else {
                if (type.equals("Chore")) {
                    if (task.getStatus() != null) {
                        String updateTask = "UPDATE Tasks SET status = '" + task.getStatus() + "' WHERE id = " + Integer.valueOf(id);
                        stmt.executeUpdate(updateTask);
                    }
                    if (task.getSize() != null) {
                        String updateTask = "UPDATE Chore SET size = '" + task.getSize() + "' WHERE taskId = " + Integer.valueOf(id);
                        stmt.executeUpdate(updateTask);
                    }
                    if (task.getDescription() != null) {
                        String updateTask = "UPDATE Chore SET description = '" + task.getDescription() + "' WHERE taskId = " + Integer.valueOf(id);
                        stmt.executeUpdate(updateTask);
                    }
                } else {
                    if (type.equals("HomeWork")) {
                        if (task.getStatus() != null) {
                            String updateTask = "UPDATE Tasks SET status = '" + task.getStatus() + "' WHERE id = " + Integer.valueOf(id);
                            stmt.executeUpdate(updateTask);
                        }
                        if (task.getCourse() != null) {
                            String updateTask = "UPDATE HomeWork SET course = '" + task.getCourse() + "' WHERE taskId = " + Integer.valueOf(id);
                            stmt.executeUpdate(updateTask);
                        }
                        if (task.getDueDate() != null) {
                            String updateTask = "UPDATE HomeWork SET dueDate = '" + task.getDueDate() + "' WHERE taskId = " + Integer.valueOf(id);
                            stmt.executeUpdate(updateTask);
                        }
                        if (task.getDetails() != null) {
                            String updateTask = "UPDATE HomeWork SET details = '" + task.getDetails() + "' WHERE taskId = " + Integer.valueOf(id);
                            stmt.executeUpdate(updateTask);
                        }
                    } else {
                        stmt.close();
                        throw new RuntimeException("Type is not suported");
                    }
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getTaskById(id);
    }

    
    private CompletionStage<GenericTask> getTaskById(String id) {
        GenericTask genericTask = null;
        String sqlSelectTask = "SELECT * FROM Tasks WHERE id = " + Integer.valueOf(id);
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Tasks WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A task with the id '" + id + "' does not exist");
            }
            ResultSet task = stmt.executeQuery(sqlSelectTask);
            String type = task.getString("type");
            String ownerId = task.getString("ownerId");
            String status = task.getString("status");
            String selectFromTaskTable = "SELECT * FROM '" + type + "' WHERE taskId = " + Integer.valueOf(id);
            ResultSet tableTask = stmt.executeQuery(selectFromTaskTable);
            if (type.equals("Chore")) {
                genericTask = new GenericTask(id, ownerId, status, type, tableTask.getString("description"), tableTask.getString("size"), null, null, null);
            } else {
                if (type.equals("HomeWork")) {
                    genericTask = new GenericTask(id, ownerId, status, type, null, null, tableTask.getString("course"), tableTask.getString("dueDate"), tableTask.getString("details"));
                }
                else {
                    throw new RuntimeException("Type " + type + " not supported");
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(genericTask);
    }

    private CompletionStage<Done> deleteTaskById(String id) {
        String sqlQuery = "DELETE FROM Tasks WHERE id = " + Integer.valueOf(id);
        String sqlSelectTask = "SELECT * FROM Tasks WHERE id = " + Integer.valueOf(id);
        try{
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Tasks WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A task with the id '" + id + "' does not exist");
            }
            ResultSet task = stmt.executeQuery(sqlSelectTask);
            String type = task.getString("type");
            String deleteFromTaskTable = "DELETE FROM '" + type + "' WHERE taskId = " + Integer.valueOf(id);
            stmt.executeUpdate(deleteFromTaskTable);
            stmt.executeUpdate(sqlQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private CompletionStage<Done> updateOwnerIdById(String ownerId, String id) {
        String sqlQuery = "UPDATE Tasks SET ownerId = '" + ownerId + "' WHERE id = " + Integer.valueOf(id); 
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Tasks WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A task with the id '" + id + "' does not exist");
            }
            stmt.execute(sqlQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private CompletionStage<String> getOwnerIdById(String id) {
        String ownerId = null;
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Tasks WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A task with the id '" + id + "' does not exist");
            }
            ResultSet task = stmt.executeQuery("SELECT * FROM Tasks WHERE id = " + Integer.valueOf(id));
            while (task.next()) {
                ownerId = task.getString("ownerId");
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(ownerId);
    }


    private CompletionStage<Done> deletePersonById(String id) {
        String sqlQuery = "DELETE FROM Persons WHERE id = " + Integer.valueOf(id);
        try{
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Persons WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A person with the id '" + id + "' does not exist");
            }
            stmt.executeUpdate(sqlQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private CompletionStage<Done> updateTaskStatusById(String status, String id) {
        String sqlQuery = "UPDATE Tasks SET status = '" + status + "' WHERE id = " + Integer.valueOf(id); 
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Tasks WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A task with the id '" + id + "' does not exist");
            }
            if (!(status.equals("Done") || status.equals("Active"))) {
                throw new RuntimeException("value '" + status + "' is not a legal task status");
            }
            stmt.execute(sqlQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(Done.getInstance());
    }


    private CompletionStage<Status> getStatusTaskById(String id) {
        Status statusToReturn = null;
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Tasks WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A task with the id '" + id + "' does not exist");
            }
            ResultSet task = stmt.executeQuery("SELECT * FROM Tasks WHERE id = " + Integer.valueOf(id));
            while (task.next()) {
                String status = task.getString("status");
                statusToReturn = Status.valueOf(status);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(statusToReturn);
    }

    private CompletionStage<Done> addTask(String id, GenericTask task) {
        String insertTaskQuery = "INSERT INTO Tasks (ownerId, type, status) VALUES ('" + Integer.valueOf(id) + "', '" + task.getType() + "', '" + task.getStatus() + "')";
        int maximumId = 1;
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Persons WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A person with the id '" + id + "' does not exist");
            }
            stmt.executeUpdate(insertTaskQuery);
            ResultSet maxId = stmt.executeQuery("SELECT MAX(id) AS max_id FROM Tasks");
            while (maxId.next()) {
                maximumId = maxId.getInt("max_id");
            }
            String sqlQuery = task.getType().equals("Chore")
                        ? "INSERT INTO Chore (taskId, size, description) VALUES (" + maximumId + ", '" + task.getSize() + "', '" + task.getDescription() + "')"
                        : "INSERT INTO HomeWork (taskId, course, dueDate, details) VALUES (" + maximumId + ", '" + task.getCourse() + "', '" + task.getDueDate() + "', '" + task.getDetails() + "')";
            stmt.executeUpdate(sqlQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private CompletionStage<PersonDetails> updatePersonById(Person person, String id) {
        String sqlQuery = "UPDATE Persons SET name = '" + person.getName() + "', email = '" + person.getEmail() + "', favoriteProgrammingLanguage = '" + person.getFavoriteProgrammingLanguage() + "' WHERE id = " + Integer.valueOf(id); 
        PersonDetails personDetails = null;
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Persons WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A person with the id '" + id + "' does not exist");
            }
            stmt.execute(sqlQuery);
            ResultSet activeTaskCountSet = stmt.executeQuery("SELECT COUNT(*) AS activeTaskCount FROM Tasks WHERE ownerId = '" + person.getId() + "'"); 
            int activeTaskCount = activeTaskCountSet.getInt("activeTaskCount");
            personDetails = new PersonDetails(id, person.getName(), person.getEmail(), person.getFavoriteProgrammingLanguage(), activeTaskCount);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(personDetails);
    }

    private CompletionStage<PersonDetails> getPersonById(String id) {
        PersonDetails personDetails = null;
        try {
            stmt = conn.createStatement();
            ResultSet idExistsSet = stmt.executeQuery("SELECT COUNT(*) AS idExists FROM Persons WHERE id = " + Integer.valueOf(id));
            int idExists = idExistsSet.getInt("idExists");
            if (idExists == 0) {
                stmt.close();
                throw new RuntimeException("A person with the id '" + id + "' does not exist");
            }
            ResultSet person = stmt.executeQuery("SELECT * FROM Persons WHERE id = " + Integer.valueOf(id));
            Person newPerson = null;
            while (person.next()) {
                String personId = String.valueOf(person.getInt("id"));        
                String name = person.getString("name");
                String email = person.getString("email");
                String favoriteProgrammingLanguage = person.getString("favoriteProgrammingLanguage");
                newPerson = new Person(personId, name, email, favoriteProgrammingLanguage);
            }
            ResultSet activeTaskCountSet = stmt.executeQuery("SELECT COUNT(*) AS activeTaskCount FROM Tasks WHERE ownerId = '" + newPerson.getId() + "'"); 
            int activeTaskCount = activeTaskCountSet.getInt("activeTaskCount");
            personDetails = new PersonDetails(id, newPerson.getName(), newPerson.getEmail(), newPerson.getFavoriteProgrammingLanguage(), activeTaskCount);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(personDetails);
    }
        
    private CompletionStage<List<PersonDetails>> getPeopleList() {
        List<Person> personsList = new LinkedList<>();
        List<PersonDetails> personDetailsList = new LinkedList<>();
        try {
            stmt = conn.createStatement();
            ResultSet persons = stmt.executeQuery("SELECT * FROM Persons");
            while (persons.next()) {
                String id = String.valueOf(persons.getInt("id")); 
                String name = persons.getString("name");
                String email = persons.getString("email");
                String favoriteProgrammingLanguage = persons.getString("favoriteProgrammingLanguage");
                personsList.add(new Person(id, name, email, favoriteProgrammingLanguage));
            }
            for (Person person : personsList) {
                ResultSet activeTaskCountSet = stmt.executeQuery("SELECT COUNT(*) AS activeTaskCount FROM Tasks WHERE ownerId = '" + person.getId() + "'"); 
                int activeTaskCount = activeTaskCountSet.getInt("activeTaskCount");
                personDetailsList.add(new PersonDetails(person.getId(), person.getName(), person.getEmail(), person.getFavoriteProgrammingLanguage(), activeTaskCount));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(personDetailsList);
    }

    private CompletionStage<Done> addPerson(Person person) {
        if (person.getEmail() == null || person.getName() == null || person.getFavoriteProgrammingLanguage() == null) {
            throw new RuntimeException("Missing data");
        }
        String sqlQuery = "INSERT INTO Persons (name, email, favoriteProgrammingLanguage) " + 
        "VALUES ('" + person.getName() + "', '" + person.getEmail() + "', '" + person.getFavoriteProgrammingLanguage() + "')";
        try {
            stmt = conn.createStatement();
            ResultSet emailSet = stmt.executeQuery("SELECT COUNT(*) AS email_count FROM Persons WHERE email = '" + person.getEmail() + "'");
            if (emailSet.getInt("email_count") > 0) {
                throw new RuntimeException("A person with email '" + person.getEmail() + "' already exists");
            }
            stmt.executeUpdate(sqlQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQL Error");
        }
        return CompletableFuture.completedFuture(Done.getInstance());
    }
}



