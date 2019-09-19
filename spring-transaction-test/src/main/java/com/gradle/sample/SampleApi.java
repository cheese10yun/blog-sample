package com.gradle.sample;

import com.gradle.sample.a.A;
import com.gradle.sample.a.ARepository;
import com.gradle.sample.a.AService;
import com.gradle.sample.b.BService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
@RequiredArgsConstructor
public class SampleApi {

  private final BService bService;
  private final AService aService;
  private final ARepository aRepository;


  @GetMapping
  public A create(){
      final A a = aService.aCreate();
      return a;
  }

  @GetMapping("/all")
  public List<A> get(){
    return aRepository.findAll();
  }

}
