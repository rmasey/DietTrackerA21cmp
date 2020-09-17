package server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.ResultSet;

public class Convertor {


    public static JSONObject convertToJSONObject(ResultSet resultSet) throws Exception {

        JSONObject jso = new JSONObject();
        while (resultSet.next()) {
            int numColumns = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = resultSet.getMetaData().getColumnName(i);
                jso.put(column_name, resultSet.getObject(column_name));
            }
        }
        return jso;
    }

    public static JSONArray convertToJSONArray(ResultSet resultSet) throws Exception {

        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            JSONObject jso = new JSONObject();
            int numColumns = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= numColumns; i++) {
                String column_name = resultSet.getMetaData().getColumnName(i);
                jso.put(column_name, resultSet.getObject(column_name));
            }
            jsonArray.add(jso);
        }
        return jsonArray;
    }
}
