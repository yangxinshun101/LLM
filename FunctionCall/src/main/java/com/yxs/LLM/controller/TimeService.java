package com.yxs.LLM.controller;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimeService {

    public Response getTime(Request request) {
        System.out.println("getTimeByZoneId，zoneId=" + request.zoneId);
        ZoneId zid = ZoneId.of(request.zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zid);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return new Response(zonedDateTime.format(formatter));
    }

    public record Request(@JsonProperty(required = true, value = "zoneId")
                           @JsonPropertyDescription("时区，例如 Asia/Shanghai") String zoneId) {
    }

    public record Response(String time) {
    }


}
