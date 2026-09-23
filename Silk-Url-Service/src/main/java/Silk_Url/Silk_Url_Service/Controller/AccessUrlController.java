package Silk_Url.Silk_Url_Service.Controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import Silk_Url.Silk_Url_Service.Service.Entity.AccessUrlService;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", methods = { RequestMethod.GET })
@RestController
@RequestMapping(value = "/")
public class AccessUrlController {
    private AccessUrlService urlService;
    private HttpStatus httpStatus;

    public AccessUrlController(AccessUrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping(value = "/{shortKey}")
    public ResponseEntity<Void> routeUrl(@PathVariable String shortKey, HttpServletRequest request) {
        // Get he Ip-address of the requested server -
        String ipAddress = request.getRemoteAddr();
        if (!urlService.isTokenAvailable(shortKey, ipAddress)) {
            this.httpStatus = HttpStatus.TOO_MANY_REQUESTS;
            return ResponseEntity.status(httpStatus).build();
        } // too many Requests

        String longUrl = urlService.getLongUrlFromShortkey(shortKey);

        if (longUrl == "invalid_key")
            this.httpStatus = HttpStatus.NOT_FOUND;
        else
            this.httpStatus = HttpStatus.FOUND;

        if (this.httpStatus.value() == 404) // invalid shortKey, throw 404 response
            return ResponseEntity.status(httpStatus).build();
        else
            return ResponseEntity.status(httpStatus).location(URI.create(longUrl)).build();
    }
}
