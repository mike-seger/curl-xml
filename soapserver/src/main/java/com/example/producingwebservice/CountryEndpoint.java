package com.example.producingwebservice;

import io.spring.guides.gs_producing_web_service.CountryList;
import io.spring.guides.gs_producing_web_service.CountryNameList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CountryEndpoint {
	private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

	private CountryRepository countryRepository;

	@Autowired
	public CountryEndpoint(CountryRepository countryRepository) {
		this.countryRepository = countryRepository;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "countryNameList")
	@ResponsePayload
	public CountryList getCountry(@RequestPayload CountryNameList request) {
		CountryList response = new CountryList();
		response.getCountry().addAll(countryRepository.findCountries(request.getName()));
		return response;
	}
}
