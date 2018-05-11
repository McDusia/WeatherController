package org.kaaproject.kaa.demo.datacollection;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetWeatherInfo {

        private List<String> weatherInfo = new ArrayList<>();

        private Map<String, Object> jsonToMap(String str) {
            return new Gson().fromJson(
                    str, new TypeToken<HashMap<String, Object>>() {}.getType()
            );
        }

        public List<String> getWeatherInfo() {
            String API_KEY = "f8697fe5aac119908c046fd679640fc4";
            String LOCATION = "Krakow,PL";
            String urlString = "http://api.openweathermap.org/data/2.5/weather?q="+LOCATION+"&appid="+API_KEY+"&units=metric";
            try{
                StringBuilder res = new StringBuilder();
                URL url = new URL(urlString);
                URLConnection conn = url.openConnection();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while((line = rd.readLine()) != null) {
                    res.append(line);
                }
                rd.close();

                Map<String, Object> respMap = jsonToMap(res.toString());
                System.out.println(respMap);
                Map<String, Object> mainMap = jsonToMap(respMap.get("main").toString());
                ArrayList arr = (ArrayList)respMap.get("weather");
                LinkedTreeMap map = (LinkedTreeMap) arr.get(0);

                String temperature = String.valueOf(mainMap.get("temp"));
                String humidity = String.valueOf(mainMap.get("humidity"));
                String pressure = String.valueOf(mainMap.get("pressure"));
                String description = String.valueOf(map.get("description"));

                weatherInfo.add(temperature);
                weatherInfo.add(humidity);
                weatherInfo.add(pressure);
                weatherInfo.add(description);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return weatherInfo;
        }
}

