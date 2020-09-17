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


@Path("weight/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Weight {

    @GET
    @Path("list")
    public String weightList(@CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked Weight.list()");

        int userID = User.validateSessionCookie(sessionCookie);
        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-EL\"}";
        }

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT WeightID, Date, WeightInKG, UserID FROM Weights WHERE userID = ? order by Date DESC"
            );
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();
            JSONArray newJSONArray = convertToJSONArray(resultSet);
            System.out.println(newJSONArray.toString());
            return newJSONArray.toString();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code WC-WL. \"}";
        }
    }


    @POST
    @Path("delete/{weightID}")
    public String weightDelete(@PathParam("weightID") int weightID, @CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked Weight.delete()");

        int userID = User.validateSessionCookie(sessionCookie);
        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-EL\"}";
        }
        try {
             PreparedStatement statement = Main.db.prepareStatement(
                    "DELETE FROM Weights WHERE WeightID = ?"
            );
            statement.setInt(1, weightID);
            statement.executeUpdate();
            return "{\"OK\": \"Weight has been deleted. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code WC-WD. \"}";
        }
    }


    @POST
    @Path("add")
    public String weightAdd(@FormDataParam("date") String date,
                            @FormDataParam("weightInKG") int weightInKG,
                            @CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked Weight.weightAdd()");

        int userID = User.validateSessionCookie(sessionCookie);
        if (userID == -1) {
            return "{\"Error\": \"Please log in.  Error code EC-EL\"}";
        }

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Weights (Date, WeightInKG, UserID) VALUES (?, ?, ?)"          //database sets WeightID when record created so omitted in SQL
            );
            statement.setString(1, date);
            statement.setInt(2, weightInKG);
            statement.setInt(3, userID);
            statement.executeUpdate();
            return "{\"OK\": \"Weight has been added. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something as gone wrong.  Please contact the administrator with the error code WC-WA. \"}";
        }

    }

}

