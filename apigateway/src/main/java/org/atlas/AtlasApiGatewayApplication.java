package org.atlas.engine.apigateway;

import org.atlas.engine.apigateway.filters.ErrorFilter;
import org.atlas.engine.apigateway.filters.PostFilter;
import org.atlas.engine.apigateway.filters.PreFilter;
import org.atlas.engine.apigateway.filters.RouteFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class AtlasApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtlasApiGatewayApplication.class, args);
	}

    @Bean
    public PreFilter preFilter() {
        return new PreFilter();
    }
    
    @Bean
    public PostFilter postFilter() {
        return new PostFilter();
    }
    @Bean
    public ErrorFilter errorFilter() {
        return new ErrorFilter();
    }
    @Bean
    public RouteFilter routeFilter() {
        return new RouteFilter();
    }
}
