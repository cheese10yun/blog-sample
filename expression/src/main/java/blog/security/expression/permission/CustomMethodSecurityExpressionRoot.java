package blog.security.expression.permission;

import blog.security.expression.user.MyUserPrincipal;
import blog.security.expression.user.User;
import blog.security.expression.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

import java.util.Optional;


public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;
    private UserRepository userRepository;



    public CustomMethodSecurityExpressionRoot(Authentication authentication, UserRepository userRepository) {
        super(authentication);
        this.userRepository = userRepository;
    }

    public boolean isMember(Long OrganizationId){
        User user = ((MyUserPrincipal) this.getPrincipal()).getUser();
        return user.getOrganization().getId().longValue() == OrganizationId.longValue();
    }

//    public boolean isOwner(Long targetUserId){
//        final MyUserPrincipal user = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        Long ownerId = user.getId();
//        if(targetUserId == null || ownerId == null)
//            return false;
//
//        return targetUserId.longValue() == ownerId.longValue();
//
//    }


    public boolean isOwner(Long targetUserId){
        final MyUserPrincipal user = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long ownerId = user.getId();
        if(targetUserId == null || ownerId == null)
            return false;

        final Optional<User> findUser = userRepository.findById(targetUserId);


        return findUser.get().getId().longValue() == user.getId().longValue();
    }




    @Override
    public void setFilterObject(Object obj) {
        this.filterObject = obj;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object o) {
        this.returnObject = o;

    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
