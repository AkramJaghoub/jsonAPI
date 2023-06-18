package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/**
 *
 * @author Akram
 */
public class Main {
    private static final String API_URL = "https://newsdata.io/api/1/news?apikey=pub_134477c27e30224faf4b792ace96f9d4e243c&q=football";
    private static final String JSON_FILE_PATH = "D:\\University\\adv database\\noSQLDb\\JSON.txt";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/nosql";
    private static final String DATABASE_USER = "root";

    public static void main(String[] args) {
        try {
            String response = sendGetRequest(API_URL);
            saveJsonToFile(response, JSON_FILE_PATH);
            processJsonData(JSON_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//end main

    private static String sendGetRequest(String url) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);

        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
        } else {
            throw new IOException("API request failed with status code: " + statusCode);
        }
        return null;
    } //end sendGetRequest method

    /////// Save results to json file /////
    private static void saveJsonToFile(String jsonContent, String filePath) throws IOException {
        File outputFile = new File(filePath);
        FileWriter writer = new FileWriter(outputFile, true);
        writer.write(jsonContent);
        writer.close();
    }//end saveJsonToFile method

    private static void processJsonData(String filePath) {
        try (FileReader fileReader = new FileReader(new File(filePath))) {
            JSONTokener tokener = new JSONTokener(fileReader);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray results = jsonObject.getJSONArray("results");

            int cID = 0, newsID = 0;
            for (int i = 0; i < results.length(); i++) { //for loop for results array
                JSONObject objForResultsIndex = (JSONObject) results.get(i);
                //some data may already have single quotations so they have to be removed
                String title = (String) objForResultsIndex.get("title").toString().replaceAll("'", "");
                String language = (String) objForResultsIndex.get("language");
                String source_id = (String) objForResultsIndex.get("source_id");
                String pubDate = (String) objForResultsIndex.get("pubDate");
                JSONArray category = objForResultsIndex.getJSONArray("category");

                ////////// Opening data connection to mySQL////////
                Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, "21232619boom");
                System.out.println("Connection is open");
                Statement stat = conn.createStatement();

                //To insert the data into the ftblNews table in MySQL
                String ftbNewsDataQuery = "INSERT INTO ftblNews (newsID, title, language, source_id, pubDate) VALUES (" + ++newsID + ", '" + title + "', '" + language
                        + "', '" + source_id + "', '" + pubDate + "');";
                stat.executeUpdate(ftbNewsDataQuery);

                ////////// for loop to for category array in case there more than one category in the array////////
                innerFor:
                for (int j = 0; j < category.length(); j++) {
                    String categoryDataQuery = "";
                    String query = "select * from category";
                    ResultSet rs = stat.executeQuery(query);

                      /*while loop to get the second column(categoryName)
                     from table category to check if the category data already
                     exists if it exists it breaks out of the Inner loop
                     if it doesn't it stores it inside the database*/
                    while (rs.next()) {
                        if (rs.getString(2).trim().equals(category.toString()))
                            break innerFor;
                    }//End while

                    //To insert the data into the category table in MySQL
                    categoryDataQuery = "INSERT INTO category (cID, categoryName) VALUES (" + ++cID + ", '" + category + "');";
                    stat.executeUpdate(categoryDataQuery);
                }//end InnerLoop

                //To insert the data into the hasTable table in MySQL
                String hasTableQuery = "INSERT INTO hasTable (newsID, cID) VALUES (" + newsID + ", '" + cID + "');";
                stat.executeUpdate(hasTableQuery);
                conn.close(); //to close the connection
            } //end OuterLoop
        }// end Try
        catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } //end processJsonData method
}//End class