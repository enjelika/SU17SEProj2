package systemREST;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import systemPD.Role;
import systemPD.User;
import systemPD.System;


@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
//    public void filter(ContainerRequestContext requestContext) throws IOException {
//        return;
//    }
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String username = requestContext.getSecurityContext().getUserPrincipal().getName();
        
      
        // Get the resource class which matches with the requested URL
        // Extract the roles declared by it
        Class<?> resourceClass = resourceInfo.getResourceClass();
        List<Role> classRoles = extractRoles(resourceClass);

        // Get the resource method which matches with the requested URL
        // Extract the roles declared by it
        Method resourceMethod = resourceInfo.getResourceMethod();
        List<Role> methodRoles = extractRoles(resourceMethod);

        try {

            // Check if the user is allowed to execute the method
            // The method annotations override the class annotations
            if (methodRoles.isEmpty()) {
                checkPermissions(classRoles,username);
            } else {
                checkPermissions(methodRoles,username);
            }

        } catch (Exception e) {
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    // Extract the roles from the annotated element
    private List<Role> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<Role>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<Role>();
            } else {
                Role[] allowedRoles = secured.value();
                return Arrays.asList(allowedRoles);
            }
        }
    }
    private void checkPermissions(List<Role> allowedRoles, String username) throws Exception {
      // Check if the user contains one of the allowed roles
      // Throw an Exception if the user has not permission to execute the method
     if (allowedRoles.size()==0) return;
      User user = System.findUserByUserName(username);
      for (Role role : allowedRoles) {
        if (user.isAuthorize(role)) return;
      }
      throw new Exception();
  }
}
