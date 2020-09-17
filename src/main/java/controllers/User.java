package controllers;

import server.Main;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static server.Convertor.convertToJSONObject;


@Path("user/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)


public class User {

    @POST
    @Path("login")
    public String userLogin(@FormDataParam("username") String username, @FormDataParam("password") String password) {
        System.out.println("Invoked User.userLogin() with username of " + username + " and password " + password);

        int userID = getUserID(username, password);
        System.out.println("UserID is " + userID);
        if (userID == -1) {
            return "{\"Error\": \"Username or password is incorrect.  Are you sure you've registered? \"}";
        }
        String uuid = UUID.randomUUID().toString();                 //create a unique ID for session
        String result = updateUUIDinDB(userID, uuid);               //store UUID for the user in the database
        System.out.println("Generated uuid of " + uuid);
        if (result.equals("OK")) {
            JSONObject userDetails = new JSONObject();
            userDetails.put("sessionToken", uuid);
            return userDetails.toString();
        } else {
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UL. \"}";
        }
    }


    public static int getUserID(String username, String password) {

        System.out.println("Invoked User.getUserID()");
        try {
            PreparedStatement statement = Main.db.prepareStatement("SELECT UserID FROM Users WHERE Username = ? AND Password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            //ResultSet doesn't provide any isEmpty(), if next() return false it means ResultSet is empty
            if (resultSet.next()==false){
                return -1 ;
            } else {
                return resultSet.getInt("UserID");
            }



            //            Statement statement = DatabaseConnection.connection.createStatement();        //to test this make connection public in DBConnection class
//            String query = "SELECT UserID FROM Users WHERE Password = '"+ password+"'" ;
            //now user can enter      b' or '1'='1    evaluates to true so all records turned and they get logged in as the last result in resultsSet - ha ha ha
            //this won't work with prepared statement as all of       b' or '1'='1    is taken as the password
//            ResultSet resultSet = statement.executeQuery(query);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }



    public static String updateUUIDinDB(int userID, String UUID) {

        System.out.println("Invoked User.updateUUIDinDB()");

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "UPDATE Users SET UUID = ? WHERE UserID = ?"
            );
            statement.setString(1, UUID);
            statement.setInt(2, userID);
            statement.executeUpdate();
            return "OK";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Error";
        }
    }


    public static int validateSessionCookie(Cookie sessionCookie) {     //returns the userID that of the record with the cookie value

        String uuid = sessionCookie.getValue();
        System.out.println("Invoked User.validateSessionCookie(), cookie value " + uuid);

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT UserID FROM Users WHERE UUID = ?"
            );
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("userID is " + resultSet.getInt("UserID"));
            return resultSet.getInt("UserID");  //Retrieve by column name  (should really test we only get one result back!)
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;  //rogue value indicating error

        }
    }


    @POST
    @Path("add")
    public String userAdd(@FormDataParam("firstName") String firstName, @FormDataParam("lastName") String lastName,
                          @FormDataParam("targetWeight") int targetWeight, @FormDataParam("maxCalories") int caloriesPerDay,
                          @FormDataParam("gender") String gender, @FormDataParam("userName") String username,
                          @FormDataParam("password") String password) {

        System.out.println("Invoked User.userAdd()");

        //would be better to test if username taken and if username and password already exist and return useful error message to browswer.

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Users (FirstName, LastName, TargetWeight, CaloriesPerDay, Gender, Username, Password, UUID, ImageLink) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setInt(3, targetWeight);
            statement.setInt(4, caloriesPerDay);
            statement.setString(5, gender);
            statement.setString(6, username);
            statement.setString(7, password);
            statement.setString(8, "none");
            statement.setString(9, "none.png");
            statement.executeUpdate();
            return "{\"OK\": \"New user has been added successfully. \"}";

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UA. \"}";
        }
    }


    @GET
    @Path("name")
    public String userName(@CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked User.userName()");

        if (sessionCookie == null) {
            return "Error: Something as gone wrong.  Please contact the administrator with the error code UC-UN";
        }

        try {
            String uuid = sessionCookie.getValue();
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT FirstName FROM Users WHERE UUID = ?"
            );
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getString("FirstName");
        } catch (Exception e) {
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UN. \"}";
        }

    }


    @GET
    @Path("get")
    public String userGet(@CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked User.userGet()");

        if (sessionCookie == null) {
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UG. \"}";
        }

        try {
            String uuid = sessionCookie.getValue();
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT * FROM Users WHERE UUID = ?"
            );
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            JSONObject resultsJSON = convertToJSONObject(resultSet);
            return resultsJSON.toString();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UG. \"}";
        }
    }

    @POST
    @Path("update")
    public String userUpdate(@CookieParam("sessionToken") Cookie sessionCookie,
                             @FormDataParam("firstName") String firstName,
                             @FormDataParam("lastName") String lastName,
                             @FormDataParam("targetWeight") int targetWeight,
                             @FormDataParam("caloriesPerDay") int caloriesPerDay,
                             @FormDataParam("gender") String gender,
                             @FormDataParam("username") String username,
                             @FormDataParam("password") String password) {

        System.out.println("Invoked User.userUpdate()");

        if (sessionCookie == null) {
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UU. \"}";
        }
        try {
            int userID = User.validateSessionCookie(sessionCookie);

            PreparedStatement statement = Main.db.prepareStatement(
                    "UPDATE Users SET FirstName = ?, LastName = ?, TargetWeight = ?, CaloriesPerDay = ?, Gender = ?, Username = ?, Password = ? WHERE UserID = ?"
            );
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setInt(3, targetWeight);
            statement.setInt(4, caloriesPerDay);
            statement.setString(5, gender);
            statement.setString(6, username);
            statement.setString(7, password);
            statement.setInt(8, userID);
            statement.executeUpdate();
            return "OK";
        } catch (Exception e) {
            System.out.println(e.getMessage());   //print the exception error message so it can be used to rectify the problem.  Do not send this back to client!
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UU. \"}";
        }
    }


    @POST
    @Path("image")
    public String userImage(@CookieParam("sessionToken") Cookie sessionCookie,
                            @FormDataParam("file") InputStream uploadedInputStream,
                            @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception {

        System.out.println("Invoked User.userImage()");

        String fileName = fileDetail.getFileName();  //file name submitted through form
        int dot = fileName.lastIndexOf('.');            //find where the . is to get the file extension
        String fileExtension = fileName.substring(dot + 1);   //get file extension from fileName
        String newFileName = "client/img/" + UUID.randomUUID() + "." + fileExtension;  //create a new unique identifier for file and append extension

        int userID = validateSessionCookie(sessionCookie);  //validate UUID sent from browser to get userID

        if (userID == -1) {
            return "Error:  Could not validate user";
        }

        PreparedStatement statement = Main.db.prepareStatement(
                "UPDATE Users SET ImageLink = ? WHERE UserID = ?"
        );
        statement.setString(1, newFileName);
        statement.setInt(2, userID);
        statement.executeUpdate();


        String uploadedFileLocation = "C:\\Users\\Rachel\\Desktop\\IdeaProjects\\DietTracker2019v2\\resources\\" + newFileName;

        try {
            int read = 0;
            byte[] bytes = new byte[1024];
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
            System.out.println("File uploaded to server and imageLink saved to database");
            return "File uploaded to server and imageLink saved to database";

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UI. \"}";
        }


    }

    @POST
    @Path("delete")
    public String userDelete(@CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked User.userDelete()");

        try {
            PreparedStatement statement = Main.db.prepareStatement("DELETE FROM Users WHERE UUID = ?");
            statement.setString(1, sessionCookie.getValue());
            statement.executeUpdate();
            return "{\"OK\": \"User deleted. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code UC-UD. \"}";
        }

    }


}

