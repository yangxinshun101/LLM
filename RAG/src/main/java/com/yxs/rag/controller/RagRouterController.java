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

}
