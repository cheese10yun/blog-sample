package yun.blog.exception.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class MemberTest {

  @Test
  public void throws_던지기() throws JsonProcessingException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final Member member = new Member("yun");
    final String valueAsString = objectMapper.writeValueAsString(member);

  }

  @Test
  public void try_catch_감싸기() {
    final ObjectMapper objectMapper = new ObjectMapper();
    final Member member = new Member("yun");
    final String valueAsString;
    try {
      valueAsString = objectMapper.writeValueAsString(member);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }


}