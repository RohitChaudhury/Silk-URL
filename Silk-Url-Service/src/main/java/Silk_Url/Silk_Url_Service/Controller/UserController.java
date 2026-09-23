package Silk_Url.Silk_Url_Service.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import Silk_Url.Silk_Url_Service.Model.Response.Response;
import Silk_Url.Silk_Url_Service.Service.Entity.UserService;
import Silk_Url.Silk_Url_Service.Service.Response.ResponseBuilderService;

@CrossOrigin(origins = "http://localhost:3000", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE })
@RestController
@RequestMapping(value = "/user")
public class UserController {
    private UserService userService;
    private ResponseBuilderService responseBuilder;

    public UserController(UserService service, ResponseBuilderService responseBuilder) {
        this.userService = service;
        this.responseBuilder = responseBuilder;
    }

    // method to register a Particular user and generate it's JWT bearer Token
    @PostMapping(value = "/create")
    public ResponseEntity<Response> createUser(@RequestBody Users users) {
        String[] message = userService.createUser(users);
        HttpStatus http = null;
        Response response = null;

        if (message[0] == "created") {
            String jwtToken = userService.getJwtTokenForUser(users);
            response = responseBuilder.userCreatedResponse(users, jwtToken);
            http = HttpStatus.CREATED;
        } else if (message[0] == "unexpectedError") {
            response = responseBuilder.unexpectedErrorRepsponse();
            http = HttpStatus.EXPECTATION_FAILED;
        } else {
            response = responseBuilder.userFieldErrorResponse(message);
            http = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(response, http);
    }

    // method to get the Jwt token for the authenticated user
    @PostMapping(value = "/get-token")
    public ResponseEntity<Response> getToken(@RequestBody Users users) {
        String[] message = userService.verifyRegisteredUser(users);
        HttpStatus http = null;
        Response response = null;

        if (message[0] == "non-verified") {
            response = responseBuilder.unAuthorisedUserResponse();
            http = HttpStatus.UNAUTHORIZED;
        } else if (message[0] == "verified") {
            String jwtToken = userService.getJwtTokenForUser(users);
            response = responseBuilder.userVerifiedResponse(users, jwtToken);
            http = HttpStatus.CREATED;
        } else { // empty credentials response
            response = responseBuilder.userFieldErrorResponse(message);
            http = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(response, http);
    }
}
