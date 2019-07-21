package com.example.houses;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.stream.Stream;

@SpringBootApplication
public class HousesApplication {

	public static void main(String[] args) {
		SpringApplication.run(HousesApplication.class, args);
	}

	@Bean
	public CommandLineRunner houses(HouseRespository houseRespository,
									PubSubTemplate pubSubTemplate) {
		return args -> {
			Stream.of(new House("Rua Tijuca 160 Leblon"),
					new House("Rua Professor Aldo Zanini 873 Vila Operária"),
					new House("Avenida Alameda Oscar Niermayer 119 Nova Lima"),
					new House("North Pole"))
					.forEach(houseRespository::save);
			houseRespository.findAll()
					.forEach(house -> pubSubTemplate.publish("newHouses", house.getAddress(),
							null));
			;
		};
	}
}

interface HouseRespository extends CrudRepository<House, Long>{	}

@Entity
class House{

	@Id
	@GeneratedValue
	private Long id;
	private String address;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {

		return address;
	}

	public House() {
	}

	public House( String address) {
		this.address = address;
	}
}

