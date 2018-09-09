package blog.security.expression.web;

import blog.security.expression.organization.Organization;
import blog.security.expression.organization.OrganizationRepository;
import blog.security.expression.user.User;
import blog.security.expression.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
public class MainController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;


//    @PreAuthorize("hasPermission(#id, 'Foo', 'read')")

    @PostAuthorize("hasPermission(returnObject, 'read')")
//    @PreAuthorize("hasPermission(#id, 'User', 'read')")
    @RequestMapping(method = RequestMethod.GET, value = "/foos/{id}")
    public User findById(@PathVariable("id") long id){

        final Optional<User> user = userRepository.findById(id);
        return user.get();
    }

    @PreAuthorize("hasPermission(#foo, 'write')")
    @RequestMapping(method = RequestMethod.POST, value = "/foos")
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        return user;
    }


    @PreAuthorize("isMember(#id)")
    @RequestMapping(method = RequestMethod.GET, value = "/organizations/{id}")
    public Organization findOrgById(@PathVariable long id) {
        final Optional<Organization> organization = organizationRepository.findById(id);
        return organization.get();
    }


//    @PostAuthorize("hasPermission(returnObject, #id)")
    @PostAuthorize("isOwner(returnObject.id)")
    @RequestMapping(method = RequestMethod.GET, value = "users/{id}")
    public User findUserById(@PathVariable("id") long id){
        final Optional<User> user = userRepository.findById(id);
        return user.get();
    }


    @PreAuthorize("isOwner(#id)")
    @RequestMapping(method = RequestMethod.GET, value = "users/{id}/test")
    public User findUserById2(@PathVariable("id") long id){
        final Optional<User> user = userRepository.findById(id);
        return user.get();
    }


//    @PostAuthorize("isOwner(returnObject.id)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET, value = "users/{id}/admin")
    public User findUserByIdAdmin(@PathVariable("id") long id){
        final Optional<User> user = userRepository.findById(id);
        return user.get();

    }




}
