package com.tarantula.cci.http;

import com.icodesoftware.Session;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.tarantula.cci.HttpDispatcher;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.metrics.DistributionMetricsService;

import java.io.IOException;

public class MetricsScraperHandler extends HttpDispatcher {

    private TarantulaContext _context;
    private DistributionMetricsService _metricsService;
    private DeploymentServiceProvider _deploymentService;

    public MetricsScraperHandler(MetricsListener metricsListener) {
        super(metricsListener);
        _context = TarantulaContext.getInstance();
        _metricsService = _context.clusterProvider().serviceProvider(DistributionMetricsService.NAME);
        _deploymentService = _context.clusterProvider().serviceProvider(DeploymentServiceProvider.NAME);
    }

    @Override
    public void resource(EndPoint.Resource resource) {

    }

    @Override
    public String path() {
        return RequestHandler.METRICS_PATH;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        var output = new StringBuilder();

        var newline = "\n";
        output.append("<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">");

        var services = _context.serviceViewList;


        for(var key : _context.metricsList())
        {
            var metrics = _context.metrics(key);
//            var catagories = metrics.categories();

            var stats = metrics.statistics();
            var statsdata = stats.summary();
            for(var stat : statsdata)
            {
                output.append(key.replace('-', '_')).append("_")
                        .append(stat.name()).append(' ')
                        .append(stat.hourly()).append(newline);
            }
            output.append(newline);
        }

        output.append("</pre>");

        byte[] data = output.toString().getBytes();

        httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"text/html");
        httpExchange.sendResponseHeaders(200,data.length);
        httpExchange.getResponseBody().write(data);
        httpExchange.close();
    }
}
