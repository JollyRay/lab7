package com.company.lab5;

import com.company.Server.json.JSONEntity;
import com.company.Server.json.JSONObject;
import com.company.Server.json.JSONParser;

import java.time.ZonedDateTime;

public class HumanMaker {
    public static Human makeHuman(String JSONelement, String login) throws Exception {
        ZonedDateTime time;
        JSONEntity entity = JSONParser.parse(JSONelement);
        if (entity == null) throw new Exception("Следует указать человечка");
        JSONObject obj = entity.toObject(new Exception("Вы ввели " + entity.getTypeName() + ", а нужен объект"));
        if (obj.getItem("name")==null) throw new Exception("Следует ввести имя");
        String name = obj.getItem("name").toString(new Exception("Вы ввели не строку (1 параметр)")).getContent();
        if (obj.getItem("clothes")==null) throw new Exception("Следует ввести одетость");
        int cloth = obj.getItem("clothes").toInt(new Exception("Одетость не целове число (2 параметр)"));
        if (obj.getItem("time")==null) time = ZonedDateTime.now(); else
        time = ZonedDateTime.parse(obj.getItem("time").toString(new Exception("Вы ввели не строку (5 параметр)")).getContent());
        if (obj.getItem("height")!=null && obj.getItem("width")!=null){
            int height = obj.getItem("height").toInt(new Exception("Высота введена не как число (3 параметр)"));
            int width = obj.getItem("width").toInt(new Exception("Ширина введене не как число (4 параметр)"));
            Forwarder forw = new Forwarder(name, cloth, ZonedDateTime.now(), login);
            forw.setFind(new Picture(height, width));
            return forw;
        }
        return new Human(name, cloth, time, login) {
            @Override
            public String Go() {
                return null;
            }

            @Override
            public String Go(String str) {
                return null;
            }
        };
    }
}
