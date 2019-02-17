package yun.blog.mvc.config;

import org.springframework.format.Formatter;
import yun.blog.mvc.sample.Person;

import java.util.Locale;

public class PersonFormatter  implements Formatter<Person> {

    @Override
    public Person parse(String name, Locale locale) {
        return new Person(name, name);
    }

    @Override
    public String print(Person person, Locale locale) {
        return person.toString();
    }
}
