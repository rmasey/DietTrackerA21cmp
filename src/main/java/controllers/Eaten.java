package controllers;

import org.glassfish.jersey.media.multipart.FormDataParam;
import server.Main;

import org.json.simple.JSONArray;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static server.Convertor.convertToJSONArray;


@Path("eaten/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Eaten {

    @GET
    @Path("list/{dateEaten}")
    public String eatenList(@PathParam("dateEaten") String dateEaten, @CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked Eaten.eatenList() + dateEaten= " + dateEaten + " and sessionCookie= " + sessionCookie.getValue());

        int userID = User.validateSessionCookie(sessionCookie);
        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-EL\"}";
        }

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT Eaten.EatenID, Eaten.MealName, Eaten.Serving, Eaten.DateEaten, " +
                            "Eaten.FoodID, " +  "Eaten.UserID, " +
                            "CalsPerServing * Serving AS TotalCals, " +
                            "round(FatPerServing * Serving, 2) AS TotalFat, " +
                            "round(SatFatPerServing * Serving, 2) AS TotalSatFat, " +
                            "round(CarbsPerServing * Serving, 2) AS TotalCarbs, " +
                            "round(SugarsPerServing * Serving, 2) AS TotalSugar, " +
                            "round(FibrePerServing * Serving, 2) AS TotalFibre, " +
                            "round(ProteinPerServing * Serving, 2) AS TotalProtein, " +
                            "round(SaltPerServing * Serving, 2) AS TotalSalt " +
                            "FROM Eaten JOIN Foods ON (Foods.FoodID = Eaten.FoodID) " +
                            "WHERE userID = ? AND DateEaten = ?"
            );
            statement.setInt(1, userID);
            statement.setString(2, dateEaten);
            ResultSet resultSet = statement.executeQuery();

            JSONArray response = convertToJSONArray(resultSet);   //convert ResultSet to JSON array for the browser
            System.out.println(response.toString());              //output so we can see the results on the console
            return response.toString();

        } catch (Exception e) {
            System.out.println(e.getMessage());   //print the exception error message so it can be used to rectify the problem.  Do not send this back to client!
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code EC-EL. \"}";
        }
    }


    @POST
    @Path("delete/{eatenID}")
    public String eatenDelete(@PathParam("eatenID") int eatenID, @CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked Eaten.eatenDelete() with eatenID "+ eatenID);

        int userID = User.validateSessionCookie(sessionCookie);
        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-ED\"}";
        }

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "Delete From Eaten where EatenID = ?"
            );
            statement.setInt(1, eatenID);
            statement.executeUpdate();
            return "{\"OK\": \"Eaten has been deleted. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());   //print the exception error message so it can be used to rectify the problem.  Do not send this back to client!
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code EC-ED. \"}";

        }
    }


    @POST
    @Path("add")
    public String eatenAdd(@FormDataParam("mealName") String mealName,
                           @FormDataParam("serving") double serving,
                           @FormDataParam("dateEaten") String dateEaten,
                           @FormDataParam("foodID") String foodID,
                           @CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked Eaten.eatenAdd()");

        int userID = User.validateSessionCookie(sessionCookie);
        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-ED\"}";
        }

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Eaten (MealName, Serving, DateEaten, FoodID, UserID) VALUES ( ?, ?, ?, ?, ?)"
            );
            statement.setString(1, mealName);
            statement.setDouble(2, serving);
            statement.setString(3, dateEaten);
            statement.setString(4, foodID);
            statement.setInt(5, userID);
            statement.executeUpdate();
            return "{\"OK\": \"Eaten has been added. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());   //print the exception error message so it can be used to rectify the problem.  Do not send this back to client!
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code EC-EA. \"}";
        }

    }


}

