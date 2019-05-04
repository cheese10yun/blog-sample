package yun.blog.ddd.model;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class AddressTest {

  @Test(expected = IllegalArgumentException.class)
  public void Address_address1_비어있으면_exception() {
    Address.builder()
        .address1("")
        .address2("address 2")
        .zip("zip")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void Address_address2_비어있으면_exception() {
    Address.builder()
        .address1("address 1")
        .address2("")
        .zip("zip")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void Address_zip_비어있으면_exception() {
    Address.builder()
        .address1("address 1")
        .address2("address 2")
        .zip("")
        .build();
  }

  @Test
  public void Address_test() {
    final Address address = Address.builder()
        .address1("address 1")
        .address2("address 2")
        .zip("zip")
        .build();

    assertThat(address.getAddress1()).isEqualTo("address 1");
    assertThat(address.getAddress2()).isEqualTo("address 2");
    assertThat(address.getZip()).isEqualTo("zip");

  }
}