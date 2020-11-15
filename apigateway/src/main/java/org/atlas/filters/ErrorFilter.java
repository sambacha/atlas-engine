package org.atlas.engine.apigateway.filters;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class ErrorFilter extends ZuulFilter {

	@Override
	public String filterType() {
		return "error";
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

		StringBuilder msg = new StringBuilder("Error Filter: ");
		msg.append("Method [").append(request.getMethod()).append("] ");
		msg.append("URL [").append(request.getRequestURL().toString()).append("] ");
		if (request.getMethod().equals("PUT") || request.getMethod().equals("POST")) {
			try {
				msg.append("Body [").append(request.getReader().readLine()).append("]");
			} catch (IOException e) {
				msg.append("Body [").append(" IO ERROR WHILE READING ").append("]");
			}
		}
		System.out.println(msg.toString());
		return null;
	}
}