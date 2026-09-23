package Silk_Url.Silk_Url_Service.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import Silk_Url.Silk_Url_Service.Model.Dto.UrlDto;
import Silk_Url.Silk_Url_Service.Model.Entity.Urls;
import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import Silk_Url.Silk_Url_Service.Model.Response.Response;
import Silk_Url.Silk_Url_Service.Service.Entity.GenerateUrlService;
import Silk_Url.Silk_Url_Service.Service.Response.ResponseBuilderService;

@CrossOrigin(origins = "http://localhost:3000", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE })
@RestController
@RequestMapping(value = "/url")
public class GenerateUrlController {
    private GenerateUrlService urlService;
    private ResponseBuilderService responseBuilder;
    private Response response;

    public GenerateUrlController(GenerateUrlService urlService, ResponseBuilderService responseBuilder) {
        this.urlService = urlService;
        this.responseBuilder = responseBuilder;
    }

    @GetMapping(value = "/index")
    public ResponseEntity<Response> index() {
        this.response = responseBuilder.helloResponse();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/get-all-url")
    public ResponseEntity<Response> getAllUrls() {
        Users user = urlService.getAuthenticatedUser();
        List<UrlDto> urlDtos = urlService.getAllUrls(user.getId());
        this.response = responseBuilder.getAllUrlResponse(urlDtos);
        HttpStatus http = HttpStatus.OK;

        return new ResponseEntity<>(response, http);
    }

    @GetMapping(value = "/get-url/{id}")
    public ResponseEntity<Response> getUrlById(@PathVariable long id) {
        Users user = urlService.getAuthenticatedUser();
        UrlDto urlDto = urlService.getUrlByIdAndUser(id, user.getId());
        HttpStatus http = null;
        if (urlDto != null && urlDto.getId() != 0) {
            this.response = responseBuilder.getUrlResponse(urlDto);
            http = HttpStatus.OK;
        } else {
            this.response = responseBuilder.noUrlResponse();
            http = HttpStatus.EXPECTATION_FAILED;
        }

        return new ResponseEntity<>(this.response, http);
    }

    @PostMapping(value = "/create-url")
    public ResponseEntity<Response> createUrl(@RequestBody Urls url) {
        HttpStatus http = null;
        if (!urlService.validateUrl(url)) { // invalid Url
            this.response = responseBuilder.invalidUrlResponse();
            http = HttpStatus.BAD_REQUEST;
        } else {
            UrlDto urlDto = urlService.createUrl(url);
            if (urlDto != null && urlDto.getShortUrl() != null) {
                this.response = responseBuilder.createdUrlResponse(urlDto);
                http = HttpStatus.CREATED;
            } else { // unexpected Error Response
                this.response = responseBuilder.unexpectedErrorRepsponse();
                http = HttpStatus.EXPECTATION_FAILED;
            }
        }

        return new ResponseEntity<>(this.response, http);
    }

    @PutMapping(value = "/update-url")
    public ResponseEntity<Response> updateUrlById(@RequestBody Urls url) {
        HttpStatus http = null;

        Map<String, UrlDto> message = urlService.updateUrlById(url);

        if (message.containsKey("Invalid_url")) {
            this.response = responseBuilder.invalidUrlResponse();
            http = HttpStatus.BAD_REQUEST;
        } else if (message.containsKey("No_url")) {
            this.response = responseBuilder.noUrlResponse(url.getId());
            http = HttpStatus.EXPECTATION_FAILED;
        } else if (message.containsKey("updated")) {
            this.response = responseBuilder.updateUrlResponse(message.get("updated"));
            http = HttpStatus.OK;
        }

        return new ResponseEntity<>(response, http);
    }

    @DeleteMapping(value = "/delete-url/{id}")
    public ResponseEntity<Response> deleteUrl(@PathVariable long id) {
        HttpStatus http = null;
        String message = urlService.deleteUrlById(id);

        if (message == "invalid_id") {
            this.response = responseBuilder.noUrlResponse();
            http = HttpStatus.EXPECTATION_FAILED;
        } else if (message == "unexpected_error") {
            this.response = responseBuilder.unexpectedErrorRepsponse();
            http = HttpStatus.EXPECTATION_FAILED;
        } else if (message == "deleted") {
            this.response = responseBuilder.deleteUrlResponse();
            http = HttpStatus.OK;
        }
        return new ResponseEntity<>(response, http);
    }
}
