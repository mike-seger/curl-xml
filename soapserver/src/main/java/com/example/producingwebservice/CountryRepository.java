package com.example.producingwebservice;

import io.spring.guides.gs_producing_web_service.Country;
import io.spring.guides.gs_producing_web_service.Currency;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CountryRepository {
	private static final Map<String, Country> countries = new HashMap<>();

	@PostConstruct
	public void initData() {
		addCountry("France", "Paris", Currency.EUR, 	65273511);
		addCountry("Germany", "Berlin", Currency.EUR, 83783942);
		addCountry("Italy", "Rome", Currency.EUR, 60461826);
		addCountry("Switzerland", "Bern", Currency.CHF, 8654622);
		addCountry("Spain", "Madrid", Currency.EUR, 46754778);
		addCountry("United Kingdom", "London", Currency.EUR, 67886011);
	}

	private void addCountry(String name, String capital, Currency currency, long population) {
		Country country = new Country();
		country.setName(name);
		country.setCapital(capital);
		country.setCurrency(currency);
		country.setPopulation(population);
		countries.put(country.getName(), country);
	}

	public List<Country> findCountries(List<String> names) {
		List<Country> result = names.stream().filter(countries::containsKey).map(countries::get).collect(Collectors.toList());
		return result;
	}
}
