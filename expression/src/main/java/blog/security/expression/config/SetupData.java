package blog.security.expression.config;

import blog.security.expression.organization.Organization;
import blog.security.expression.organization.OrganizationRepository;
import blog.security.expression.privilege.Privilege;
import blog.security.expression.privilege.PrivilegeRepository;
import blog.security.expression.user.User;
import blog.security.expression.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;

@Component
@AllArgsConstructor
public class SetupData {

    private final UserRepository userRepository;
    private final PrivilegeRepository privilegeRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;



    @PostConstruct
    public void init() {
        initPrivileges();
        initOrganizations();
        initUsers();
    }

    private void initPrivileges() {
        Privilege privilege1 = new Privilege("USER_READ_PRIVILEGE");
        privilegeRepository.save(privilege1);

        Privilege privilege2 = new Privilege("USER_WRITE_PRIVILEGE");
        privilegeRepository.save(privilege2);

        Privilege privilege3 = new Privilege("ROLE_ADMIN");
        privilegeRepository.save(privilege3);

    }

    private void initOrganizations() {
        Organization org1 = new Organization("FirstOrg");
        organizationRepository.save(org1);

        Organization org2 = new Organization("SecondOrg");
        organizationRepository.save(org2);
    }

    private void initUsers() {
        Privilege privilege1 = privilegeRepository.findByName("USER_READ_PRIVILEGE");
        Privilege privilege2 = privilegeRepository.findByName("USER_WRITE_PRIVILEGE");
        Privilege privilege3 = privilegeRepository.findByName("ROLE_ADMIN");


        User user1 = new User();
        user1.setUsername("john");
        user1.setPassword(passwordEncoder.encode("123"));
        user1.setPrivileges(new HashSet<>(Arrays.asList(privilege1, privilege3)));
        user1.setOrganization(organizationRepository.findByName("FirstOrg"));
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("tom");
        user2.setPassword(passwordEncoder.encode("111"));
        user2.setPrivileges(new HashSet<>(Arrays.asList(privilege2)));
        user2.setOrganization(organizationRepository.findByName("SecondOrg"));
        userRepository.save(user2);


    }


}
