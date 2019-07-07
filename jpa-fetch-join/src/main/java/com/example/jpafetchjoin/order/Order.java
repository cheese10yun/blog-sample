package com.example.jpafetchjoin.order;


import com.example.jpafetchjoin.coupon.Coupon;
import com.example.jpafetchjoin.product.Product;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "address", nullable = false)
  private String address;

  @OneToMany(mappedBy = "order")
  private Set<Product> products = new HashSet<>();

  @OneToMany(mappedBy = "order")
  private Set<Coupon> coupons = new HashSet<>();


}
