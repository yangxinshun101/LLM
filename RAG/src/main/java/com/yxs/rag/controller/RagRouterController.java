package com.yxs.rag.controller;

import com.yxs.rag.router.GraphDatabaseService;
import com.yxs.rag.router.QueryRouteService;
import com.yxs.rag.router.VectorDatabaseService;
import com.yxs.rag.router.RelationalDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ragRouter")
public class RagRouterController {

    @Autowired
    private QueryRouteService queryRouteService;

    @Autowired
    private VectorDatabaseService vectorDatabaseService;

    @Autowired
    private GraphDatabaseService graphDatabaseService;

    @Autowired
    private RelationalDatabaseService relationalDatabaseService;

    @RequestMapping("/route")
    public String route(String query) {
        String dataBaseType = queryRouteService.route(query);

        String result;
        switch (dataBaseType) {
            case "VECTOR":
                result = vectorDatabaseService.searchVectorDatabase(query);
                break;
            case "GRAPH":
                result = graphDatabaseService.searchGraphDatabase(query);
                break;
            case "RELATIONAL":
                result = relationalDatabaseService.searchRelationalDatabase(query);
                break;
            default:
                result = "无法确定合适的数据库类型";
        }

        return String.format("Data Base Type: %s, Result: %s", dataBaseType, result);
    }

}
