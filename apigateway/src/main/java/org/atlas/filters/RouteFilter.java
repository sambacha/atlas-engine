package org.atlas.engine.apigateway.filters;

import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class RouteFilter extends ZuulFilter {

	@Override
	public String filterType() {
		return "route";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();

		StringBuilder msg = new StringBuilder("Route Filter: ");
		msg.append("Method [").append(request.getMethod()).append("] ");
		msg.append("URL [").append(request.getRequestURL().toString()).append("] ");
		System.out.println(msg.toString());
		return null;
	}
}