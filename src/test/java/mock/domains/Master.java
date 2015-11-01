package mock.domains;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;

@Entity
public class Master {
	@Id
	private String name;
	private String token;
	
	public Master() {
	}
	
	public Master(String name, String token) {
		this.name = name;
		this.token = token;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String breed) {
		this.token = breed;
	}
	
	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other, true);
	}
}
