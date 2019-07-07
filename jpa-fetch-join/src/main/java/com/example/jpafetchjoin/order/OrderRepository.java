package com.example.jpafetchjoin.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {


  @Query(
      value = "select  o from Order o "
          + "left join fetch o.coupons "
          + "left join fetch o.products ",
      countQuery = "select count(o) from Order o"
  )
  Page<Order> findByPageWithAll(Pageable pageable);


  @Query(
      value = "select  o from Order o "
          + "left join fetch o.coupons ",
      countQuery = "select count(o) from Order o"
  )
  Page<Order> findByPageWithCoupons(Pageable pageable);


  @Query(
      value = "select  o from Order o "
          + "left join fetch o.products ",
      countQuery = "select count(o) from Order o"
  )
  Page<Order> findByPageWithProducts(Pageable pageable);

}
