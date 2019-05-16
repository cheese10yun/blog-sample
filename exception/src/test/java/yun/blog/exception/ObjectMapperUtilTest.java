package yun.blog.exception;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import yun.blog.exception.member.Member;

public class ObjectMapperUtilTest {

  private ObjectMapperUtil objectMapperUtil;

  @Before
  public void setUp() throws Exception {
    objectMapperUtil = new ObjectMapperUtil();
  }

  @Test
  public void writeValueAsString_test() {
    final Member member = new Member("");
    objectMapperUtil.writeValueAsString(member);
  }

  @Test
  public void readValue_test() {
    final String json = "{\"name\": \"yun\"}";
    final Member member = objectMapperUtil.readValue(json, Member.class);
  }
}