package com.example.houses;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.StreamUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.OutputStream;
import java.util.stream.Stream;

@SpringBootApplication
public class HousesApplication {

	public static void main(String[] args) {
		SpringApplication.run(HousesApplication.class, args);
	}

	@Bean
	public CommandLineRunner houses(HouseRespository houseRespository,
									PubSubTemplate pubSubTemplate, ApplicationContext context) {
		return args -> {

			Resource houseIvana = context.getResource("https://img.olx.com.br/images/43/433915011671006.jpg");
			Resource gcsPicture = context.getResource("gs://springone-houses/houseIvana.jpg");

			byte[] pictureBytes = StreamUtils.copyToByteArray(houseIvana.getInputStream());

			try(OutputStream os = ((WritableResource) gcsPicture).getOutputStream()){
				os.write(pictureBytes);
			}
			Stream.of(new House("Rua Tijuca 160 Leblon", gcsPicture.getURI().toString()),
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
	private String gcsUri;

	public House(String address, String gcsUri) {

		this.address = address;
		this.gcsUri = gcsUri;
	}
	public House() {
	}

	public House( String address) {
		this.address = address;
	}

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

	public String getGcsUri() {
		return gcsUri;
	}

	public void setGcsUri(String gcsUri) {
		this.gcsUri = gcsUri;
	}
}

