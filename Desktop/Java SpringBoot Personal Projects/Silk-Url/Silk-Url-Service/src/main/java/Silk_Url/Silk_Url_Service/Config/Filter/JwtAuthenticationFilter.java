package Silk_Url.Silk_Url_Service.Config.Filter;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import Silk_Url.Silk_Url_Service.Service.Entity.BaseService.BaseUserDetailsService;
import Silk_Url.Silk_Url_Service.Service.Jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Filter to validate the JSON web token and the create Authentication object for the User
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private JwtService jwtService;
    private ApplicationContext appContext;

    public JwtAuthenticationFilter(JwtService jwtService, ApplicationContext appContext) {
        this.jwtService = jwtService;
        this.appContext = appContext;
    }

    // Defining the Method body for the Filter function
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse reponse, FilterChain chain)
            throws ServletException, IOException {

        // If the request is already Authenticated, skip the Filter function
        if (SecurityContextHolder.getContext().getAuthentication() != null)
            chain.doFilter(request, reponse);

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        Users user = null;

        // Check if it's a Bearer token Request
        if (authHeader != null && authHeader.startsWith("Bearer")) {
            token = authHeader.substring(7);
            username = jwtService.getUsernameFromToken(token);
        }

        // get the Users object from the untampered token
        if (username != null) {
            user = appContext.getBean(BaseUserDetailsService.class).loadUserByUsername(username);
        }

        // validate the token expiry-date and create the Authentication object of the
        // Users
        if (user != null && jwtService.validateToken(token, user)) {
            UsernamePasswordAuthenticationToken authDetails = new UsernamePasswordAuthenticationToken(user, null,
                    user.getAuthorities());
            authDetails.setDetails(new WebAuthenticationDetails(request));

            SecurityContextHolder.getContext()
                    .setAuthentication(authDetails);
        }

        chain.doFilter(request, reponse);
    }
}
