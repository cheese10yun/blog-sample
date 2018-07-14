package yun.cheese.sampleoauth2.user;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;


    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public Optional<User> getUser(@PathVariable("id") long id) {
        return userService.findById(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public User createUser(User user){
        return userService.create(user);
    }


}
