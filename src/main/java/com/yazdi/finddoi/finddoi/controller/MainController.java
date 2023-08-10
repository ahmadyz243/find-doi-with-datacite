package com.yazdi.finddoi.finddoi.controller;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping
public class MainController {

    @GetMapping
    public String viewMainPage(){
        return "index";
    }

    @GetMapping("/find-by-id")
    public ModelAndView findById(String consortiumId) {

        List<String> providers;
        try {
            providers = findProvidersWithConsotiumId(consortiumId);
            return new ModelAndView("index").addObject("providers", providers);
        } catch (IOException e) {
            return new ModelAndView("index")
                    .addObject("message", "consortium or providers was not found");
        }

    }

    public List<String> findProvidersWithConsotiumId(String consortuimId) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.datacite.org/providers?consortium-id=" + consortuimId)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();

        JSONArray dataArray;
        try {
            dataArray = new JSONObject(jsonData).getJSONArray("data");
            if (dataArray.length() == 0){
                throw new IOException();
            }
        }catch (JSONException e){
            throw new IOException();
        }

        List<String> providers = new ArrayList<>();
        for (int i = 0; i < dataArray.length(); i++) {
            providers.add(dataArray.getJSONObject(i).get("id").toString());
        }

        return providers;

    }

}
