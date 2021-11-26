/*
Developed By Ishan
 */
package com.covidtracker.covidtracker.services;
import  com.covidtracker.covidtracker.models.LocationStats;
//importing libs
import org.apache.commons.csv.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import javax.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//Fetching data from GitHub Repo
@Service
public class CoronaVirusDataService {
    //GitHub CSV file link
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private List<LocationStats> allStats = new ArrayList<>();
    public List<LocationStats> getAllStats() {
        return allStats;
     }


    @PostConstruct
    @Scheduled(cron = "1 * * * * *")
    public void fetchVirusData() throws IOException , InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        for(CSVRecord record : records){
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
            newStats.add(locationStat);

        }
        this.allStats = newStats;


    }
}
