package yun.blog.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import yun.blog.exception.exception.JsonDeserializeFailed;
import yun.blog.exception.exception.JsonSerializeFailed;

public class ObjectMapperUtil {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public String writeValueAsString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new JsonSerializeFailed(e.getMessage());
    }
  }

  public <T> T readValue(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (IOException e) {
      throw new JsonDeserializeFailed(e.getMessage());
    }
  }

}
