package yun.blog.ddd.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @NotEmpty
    @Column(name = "address1", nullable = false)
    private String address1;

    @NotEmpty
    @Column(name = "address2", nullable = false)
    private String address2;

    @NotEmpty
    @Column(name = "zip", nullable = false)
    private String zip;

    @Builder
    public Address(String address1, String address2, String zip) {
        Assert.hasText(address1, "address1 must not be empty");
        Assert.hasText(address2, "address1 must not be empty");
        Assert.hasText(zip, "address1 must not be empty");

        this.address1 = address1;
        this.address2 = address2;
        this.zip = zip;
    }
}
