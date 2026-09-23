package Silk_Url.Silk_Url_Service.Service.Entity;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import Silk_Url.Silk_Url_Service.Model.Entity.Urls;
import Silk_Url.Silk_Url_Service.Service.Entity.BaseService.BaseUrlService;
import Silk_Url.Silk_Url_Service.Service.RateLimit.RateLimitService;
import io.github.bucket4j.Bucket;

@Service
public class AccessUrlService {
    private BaseUrlService baseService;
    private RateLimitService rateLimit;

    public AccessUrlService(BaseUrlService baseService, RateLimitService rateLimit) {
        this.baseService = baseService;
        this.rateLimit = rateLimit;
    }

    // check token availability for the given shortKey and ipaddress
    public boolean isTokenAvailable(String shortKey, String ipAddr) {
        long[] tokens = baseService.getTokensFromShortkey(shortKey);
        Bucket bucketShortKey = rateLimit.resolveBucketShortKey(shortKey, tokens[1]);

        if (!bucketShortKey.tryConsume(1))
            return false;

        Bucket bucketIp = rateLimit.resolveBucketIpAddress(ipAddr, shortKey, tokens[0]);
        if (!bucketIp.tryConsume(1))
            return false;

        return true;
    }

    // method to get the Long URL from the short Key
    @Cacheable(cacheNames = BaseUrlService.CACHE_ACC_NAME, key = "#shortKey", unless = "#result == 'invalid_key'")
    public String getLongUrlFromShortkey(String shortKey) {
        try {
            if (shortKey.length() != 11)
                return "invalid_key"; // shortKey should be of 11 characters

            long id = baseService.getIdFromShortkey(shortKey);

            if (id <= 0L)
                return "invalid_key"; // Invalid id - 0 or Negative id

            Urls url = baseService.getUrlById(id);

            if (url == null || url.getLongUrl().isBlank())
                return "invalid_key"; // No Url exist with this id

            return url.getLongUrl();
        } catch (Exception error) {
            System.out.println("Unexpected Error Occured in AccessUrlService: " + error.getMessage());
            return "invalid_key";
        }
    }
}
