package weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;


public class JSonUtils {

    public static WeatherBean getWeatherBean(URL url){

        String jsonText = new JSonFetcher().getJSONText(url);
        System.out.println(jsonText);
        WeatherBean weather = new WeatherBean();

        try {
            JSONTokener jsonParser = new JSONTokener(jsonText);
            JSONObject object = (JSONObject) jsonParser.nextValue();
    //        String weatherinfo=object.getString("data");
            JSONObject details = object.getJSONObject("data");
            JSONArray jsonArray=details.getJSONArray("forecast");
            JSONObject today=(JSONObject) jsonArray.get(0);
            String city = details.getString("city");
            System.out.println(city);
            String temp1=details.getString("wendu");
            System.out.println(temp1);
            String weatherToday=today.getString("type");
            System.out.println(weatherToday);
            String date=today.getString("date");

            weather.setCity(city);
            weather.setTemp1(temp1);
            weather.setWeather1(weatherToday);
            weather.setDate(date);

        } catch (JSONException e) {
            System.out.println("test");
            e.printStackTrace();
        }

        return weather;
    }
}
