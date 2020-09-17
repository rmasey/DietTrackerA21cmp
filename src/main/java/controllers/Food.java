package controllers;

import server.Main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static server.Convertor.convertToJSONArray;


@Path("food/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Food {

    @GET
    @Path("list")
    public String foodList() {

        System.out.println("Invoked Food.foodList()");

        try {
                PreparedStatement statement = Main.db.prepareStatement(   //using public connection
                        "SELECT FoodID, ServingSize, FibrePerServing, FatPerServing, SugarsPerServing, CalsPerServing, CarbsPerServing, ProteinPerServing, SatFatPerServing, SaltPerServing FROM Foods");
                ResultSet resultSet = statement.executeQuery();
                JSONArray response = convertToJSONArray(resultSet);   //convert resultSet to JSONArray
                return response.toString();

        } catch (Exception e) {         //if there's an exception when doing the SQL query or converting to JSON print error
            System.out.println(e.getMessage());   //print the exception error message so it can be used to rectify the problem.  Do not send this back to client!
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code FC-FL. \"}";
        }

    }

    @GET
    @Path("list/{searchString}")
    public String foodList(@PathParam("searchString") String searchString) {

        System.out.println("Invoked Food.foodList() with searchString = " + searchString);

        try {

                PreparedStatement statement = Main.db.prepareStatement(   //using public connection
                        "SELECT FoodID, ServingSize, FibrePerServing, FatPerServing, SugarsPerServing, CalsPerServing, CarbsPerServing, ProteinPerServing, SatFatPerServing, SaltPerServing FROM Foods where FoodID LIKE ?");
                statement.setString(1, '%' + searchString.toLowerCase() + '%');  //% is wildcard so FoodID contains search string
                ResultSet resultSet = statement.executeQuery();
                JSONArray response = convertToJSONArray(resultSet);   //convert resultSet to JSONArray
                return response.toString();


        } catch (Exception e) {         //if there's an exception when doing the SQL query or converting to JSON print error
            System.out.println(e.getMessage());   //print the exception error message so it can be used to rectify the problem.  Do not send this back to client!
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code FC-FLS. \"}";
        }

    }


}

