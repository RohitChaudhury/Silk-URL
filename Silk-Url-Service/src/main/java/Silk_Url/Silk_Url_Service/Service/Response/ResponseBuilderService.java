package Silk_Url.Silk_Url_Service.Service.Response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import Silk_Url.Silk_Url_Service.Model.Dto.UrlDto;
import Silk_Url.Silk_Url_Service.Model.Dto.UsersDto;
import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import Silk_Url.Silk_Url_Service.Model.Response.Response;
import Silk_Url.Silk_Url_Service.Model.Response.ResponseErrorDetails;
import Silk_Url.Silk_Url_Service.Service.Entity.BaseService.BaseUrlService;

// Service class to send object of Reponse Entity
@Service
public class ResponseBuilderService {
    private Response response;
    private BaseUrlService baseUrlService;

    public ResponseBuilderService(Response response, BaseUrlService baseUrlService) {
        this.response = response;
        this.baseUrlService = baseUrlService;
    }

    // method to get DTO of the Users Entity
    private UsersDto toUsersDto(Users user, String jwtToken) {
        return new UsersDto(user.getEmail(), user.getUsername(), jwtToken, Instant.now().plus(1, ChronoUnit.DAYS));
    }

    public Response helloResponse() {
        Users user = baseUrlService.getAuthenticatedUser(); // get the Entity of the Current Authenticated User
        response.setStatus(HttpStatus.OK.getReasonPhrase());
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Hello " + user.getUsername() + ", Welcome to Silk-URL API");
        response.setResponseError("", new ArrayList<>());
        response.setData(new ArrayList<>());
        return response;
    }

    public Response userFieldErrorResponse(String[] errors) {
        List<ResponseErrorDetails> responseDetails = new ArrayList<>();
        response.setStatus(HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage("Validation Failed");

        for (String error : errors) {
            if (error == "emptyEmail")
                responseDetails.add(new ResponseErrorDetails("email", "The Field email is empty."));
            else if (error == "emptyPassword")
                responseDetails.add(new ResponseErrorDetails("password", "The Field password is empty."));
            else if (error == "emptyUsername")
                responseDetails.add(new ResponseErrorDetails("username", "The Field username is empty."));
            else if (error == "invalidEmail")
                responseDetails.add(new ResponseErrorDetails("email", "The format of field email is invalid."));
            else if (error == "userEmailExist")
                responseDetails.add(new ResponseErrorDetails("email", "An User with this email is already Exist."));
            else if (error == "userUsernameExist")
                responseDetails
                        .add(new ResponseErrorDetails("username", "This username is already taken by another user."));
            else if (error == "invalidPassword")
                responseDetails.add(new ResponseErrorDetails("password",
                        "Inavalid Password. Password must contain:" +
                                " atleast one digit [0-9]." +
                                " atleast one lowercase Latin character [a-z]." +
                                " atleast one uppercase Latin character [A-Z]." +
                                " atleast one special character like ! @ # & ( )." +
                                " a length of at least 8 characters and a maximum of 20 characters."));

        }

        response.setResponseError("Invalid Field Input", responseDetails);
        response.setData(null);
        return response;
    }

    public Response unexpectedErrorRepsponse() {
        response.setStatus(HttpStatus.EXPECTATION_FAILED.getReasonPhrase());
        response.setCode(HttpStatus.EXPECTATION_FAILED.value());
        response.setMessage("An Unexptected Error Occured in the Server");
        response.setResponseError("", new ArrayList<>());
        response.setData(null);
        return response;
    }

    public Response userCreatedResponse(Users users, String jwtToken) {
        List<UsersDto> userDtoList = new ArrayList<>();
        response.setStatus(HttpStatus.CREATED.getReasonPhrase());
        response.setCode(HttpStatus.CREATED.value());
        response.setMessage("User Created Successfully");
        response.setResponseError("", new ArrayList<>());

        // Pass the UsersDto to the response data
        userDtoList.add(this.toUsersDto(users, jwtToken));
        response.setData(userDtoList);
        return response;
    }

    public Response userVerifiedResponse(Users users, String jwtToken) {
        List<UsersDto> userDtoList = new ArrayList<>();
        response.setStatus(HttpStatus.CREATED.getReasonPhrase());
        response.setCode(HttpStatus.CREATED.value());
        response.setMessage("User Authenticated Successfully");
        response.setResponseError("", new ArrayList<>());

        // Pass the UsersDto to the response data
        userDtoList.add(this.toUsersDto(users, jwtToken));
        response.setData(userDtoList);
        return response;
    }

    public Response unAuthorisedUserResponse() {
        response.setStatus(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        response.setCode(HttpStatus.UNAUTHORIZED.value());
        response.setMessage("Un-Authorise User: Invalid Credentials");
        response.setResponseError("", new ArrayList<>());
        response.setData(null);
        return response;
    }

    public Response invalidUrlResponse() {
        List<ResponseErrorDetails> responseDetails = new ArrayList<>();
        response.setStatus(HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage("Invalid Input");
        responseDetails.add(new ResponseErrorDetails("longUrl", "Invalid URL Format or URI Syntax"));

        response.setResponseError("Invalid Field Input", responseDetails);
        response.setData(null);

        return response;
    }

    public Response createdUrlResponse(UrlDto urlDto) {
        List<UrlDto> urlDtoList = Arrays.asList(urlDto);
        response.setStatus(HttpStatus.CREATED.getReasonPhrase());
        response.setCode(HttpStatus.CREATED.value());
        response.setMessage("Short URL Created Successfully");
        response.setResponseError("", new ArrayList<>());
        response.setData(urlDtoList);
        return response;
    }

    public Response getAllUrlResponse(List<UrlDto> urlDtos) {
        response.setStatus(HttpStatus.OK.getReasonPhrase());
        response.setCode(HttpStatus.OK.value());
        response.setMessage("All URLs Fetched Successfully");
        response.setResponseError("", new ArrayList<>());
        response.setData(urlDtos);

        return response;
    }

    public Response getUrlResponse(UrlDto urlDto) {
        List<UrlDto> urlDtos = new ArrayList<>();
        urlDtos.add(urlDto);
        response.setStatus(HttpStatus.OK.getReasonPhrase());
        response.setCode(HttpStatus.OK.value());
        response.setMessage("URL fetched Succesfully");
        response.setResponseError("", new ArrayList<>());
        response.setData(urlDtos);

        return response;
    }

    public Response noUrlResponse() {
        response.setStatus(HttpStatus.EXPECTATION_FAILED.getReasonPhrase());
        response.setCode(HttpStatus.EXPECTATION_FAILED.value());
        response.setMessage("No URL exist by this id");
        response.setResponseError("", new ArrayList<>());
        response.setData(new ArrayList<>());
        return response;
    }

    public Response updateUrlResponse(UrlDto urlDto) {
        List<UrlDto> urlDtos = new ArrayList<>();
        urlDtos.add(urlDto);
        response.setStatus(HttpStatus.OK.getReasonPhrase());
        response.setCode(HttpStatus.OK.value());
        response.setMessage("URL Updated Successfully");
        response.setResponseError("", new ArrayList<>());
        response.setData(urlDtos);
        return response;
    }

    public Response noUrlResponse(long id) {
        List<ResponseErrorDetails> responseDetails = new ArrayList<>();
        response.setStatus(HttpStatus.EXPECTATION_FAILED.getReasonPhrase());
        response.setCode(HttpStatus.EXPECTATION_FAILED.value());
        response.setMessage("No URL exist");
        responseDetails.add(new ResponseErrorDetails("id", "Invalid id or No URL exist by this id " + id));
        response.setResponseError("Invalid Input", responseDetails);
        response.setData(new ArrayList<>());
        return response;
    }

    public Response deleteUrlResponse() {
        response.setStatus(HttpStatus.OK.getReasonPhrase());
        response.setCode(HttpStatus.OK.value());
        response.setMessage("URL deleted Successfully");
        response.setResponseError("", new ArrayList<>());
        response.setData(new ArrayList<>());
        return response;
    }
}