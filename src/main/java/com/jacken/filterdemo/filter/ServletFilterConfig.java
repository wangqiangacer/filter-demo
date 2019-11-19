package com.jacken.filterdemo.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "access-no-auth")
public class ServletFilterConfig {

	private List<String> uriList;

	public List<String> getUriList() {
		return uriList;
	}

	public void setUriList(List<String> uriList) {
		this.uriList = uriList;
	}
	 
	
}
