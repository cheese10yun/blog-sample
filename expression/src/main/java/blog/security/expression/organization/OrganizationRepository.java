package blog.security.expression.organization;

import blog.security.expression.privilege.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Organization findByName(String name);
}
