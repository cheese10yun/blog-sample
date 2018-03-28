package com.cheese.embedded.domain;


import com.cheese.embedded.model.Address;
import com.cheese.embedded.model.Name;
import com.cheese.embedded.model.PhoneNumber;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receiver {

    @Id
    @GeneratedValue
    private long id;

    @Embedded
    private Name name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "company_city")),
            @AttributeOverride(name = "street", column = @Column(name = "company_street")),
            @AttributeOverride(name = "zip_code", column = @Column(name = "company_zip_code"))
    })
    private Address address;

    @Embedded
    private PhoneNumber phoneNumber;

    @Builder
    public Receiver(Name name, Address address, PhoneNumber phoneNumber) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public void updateAddress(Address address) {
        this.address = address;
    }

    public void updatePhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
