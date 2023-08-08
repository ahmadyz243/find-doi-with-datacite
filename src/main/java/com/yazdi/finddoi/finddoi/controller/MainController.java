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
    public ModelAndView findById(String doiPrefix, String doiSuffix) {

        String providerId;
        List<String> clients = new ArrayList<>();

        try {
            providerId = findProviderWithDoi(doiPrefix, doiSuffix);
            clients = findProviderClients(providerId);
        } catch (IOException e) {
            return new ModelAndView("index")
                    .addObject(
                            "provider"
                            , "DOI or provider was not found!!!")
                    .addObject("clients", clients);
        }

        return new ModelAndView("index")
                .addObject(
                        "provider"
                        , "the provider id is " + providerId)
                .addObject("clients", clients);

    }

    public String findProviderWithDoi(String doiPrefix, String doiSuffix) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.datacite.org/dois/" + doiPrefix + "/" + doiSuffix)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();
        JSONObject data;

        try {
            data =  (JSONObject) new JSONObject(jsonData).get("data");
        }catch (JSONException e){
            throw new IOException();
        }

        /*
        JSONArray jsonArray = jsonObject.getJSONArray("data");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object     = jsonArray.getJSONObject(i);
        }
        */

        JSONObject relationships = new JSONObject(data.get("relationships").toString());
        JSONObject provider = new JSONObject(relationships.get("provider").toString());
        JSONObject providerData = new JSONObject(provider.get("data").toString());
        return (String) providerData.get("id");

    }

    public List<String> findProviderClients(String providerId) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.datacite.org/clients?provider-id=" + providerId)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();
        JSONArray data = (JSONArray) new JSONObject(jsonData).get("data");
        List<String> clientIds = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            clientIds.add(
                    data.getJSONObject(i).get("id").toString()
            );
        }

        return clientIds;

    }

}
