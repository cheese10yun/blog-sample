package com.example.jpafetchjoin.address;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "city", nullable = false)
  private String city;

  public Address(String city) {
    this.city = city;
  }

}
