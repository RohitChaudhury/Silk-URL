package Silk_Url.Silk_Url_Service.Service.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import Silk_Url.Silk_Url_Service.Model.Dto.UrlDto;
import Silk_Url.Silk_Url_Service.Model.Entity.Urls;
import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import Silk_Url.Silk_Url_Service.Service.Entity.BaseService.BaseUrlService;

@Service
public class GenerateUrlService {
    private BaseUrlService baseService;

    public GenerateUrlService(BaseUrlService baseService) {
        this.baseService = baseService;
    }

    // get the Authenicated user from the Base Service
    public Users getAuthenticatedUser() {
        return baseService.getAuthenticatedUser();
    }

    // method to validate Url
    public boolean validateUrl(Urls urls) {
        return baseService.isValidNativeUrl(urls.getLongUrl());
    }

    // method to perform cache operations when Url is created
    private void performCacheOperationsOnUrlCreate(long userId, Urls url) {
        // update Url Access Cache
        baseService.updateUrlAccessCache(url.getShortKey(), url.getLongUrl()); // enter shortkey and longUrl in
        // Cache
        long[] tokens = { url.getIpAddressTokens(), url.getUrlTokens() };
        baseService.updateUrlAccessUrlTokensCache(url.getShortKey(), tokens); // enter shortKey and token for url in
        // Cache
        baseService.removeUrlGenerateAllUrlsCache(userId); // remove Url generate All urls Cache
    }

    // method to perform cache opertatons when an Url is updated
    public void performCacheOperationsOnUrlUpdate(long userId, UrlDto urlDto, Urls url, Urls newUrl) {
        // Perform Url Access Cache Updates
        baseService.updateUrlAccessCache(newUrl.getShortKey(), newUrl.getLongUrl());
        long[] tokens = { newUrl.getIpAddressTokens(), newUrl.getUrlTokens() };
        baseService.updateUrlAccessUrlTokensCache(newUrl.getShortKey(), tokens);
        baseService.removeUrlAccessUrlBucketConfigCache(url.getIpAddressTokens());
        baseService.removeUrlAccessUrlBucketConfigCache(url.getUrlTokens());

        // Perform Url Generate Cache Updates
        baseService.updateUrlGenerateUpdateCache(urlDto.getId(), userId, urlDto);
        baseService.removeUrlGenerateAllUrlsCache(userId);

        // remove bucket cache for all shortkeys
        baseService.removeBucketCache();
    }

    // method to perform cache when url is deleted
    public void performCacheOperationsOnUrlDelete(Urls url) {
        // remove url form Access url cache
        baseService.removeUrlAccessCache(url.getShortKey());
        baseService.removeUrlAccessUrlTokensCache(url.getShortKey());
        baseService.removeUrlAccessUrlBucketConfigCache(url.getIpAddressTokens());
        baseService.removeUrlAccessUrlBucketConfigCache(url.getUrlTokens());

        // remove url from Generate url cache
        baseService.removeUrlGenerateUpdateCache(url.getId(), url.getUserId());
        baseService.removeUrlGenerateAllUrlsCache(url.getUserId());

        // remove bucket cache for all shortkeys
        baseService.removeBucketCache();
    }

    // method to get all the Urls of an authenticated user
    @Cacheable(cacheNames = BaseUrlService.CACHE_GEN_NAME, key = "#id")
    public List<UrlDto> getAllUrls(long id) {
        List<Urls> urls = baseService.getAllUsersUrls(id); // get the List of Urls by the User
        List<UrlDto> urlDtos = baseService.getUrlDtoFromUrls(urls);

        return urlDtos;
    }

    // method to get an Url by Id
    @Cacheable(cacheNames = BaseUrlService.CACHE_GEN_NAME, key = "#userId + ':' + #id", unless = "#result == null")
    public UrlDto getUrlByIdAndUser(long id, long userId) {
        Urls url = baseService.getUrlByIdAndUser(id, userId);
        return url != null && url.getId() != 0
                ? baseService.getUrlDto(url)
                : new UrlDto();
    }

    // method to create a short URL from the long URL called by Controller
    public UrlDto createUrl(Urls url) {
        try {
            Users user = baseService.getAuthenticatedUser();
            url.setUserId(user.getId()); // set user id in Url entity

            // Generate Key -
            long lastId = baseService.getCurrentEntityDbId();
            String key = baseService.generateKey(lastId);

            url.setShortKey(key); // set shortkey in Url entity

            // set the rateLimit to 60 per minute to Ip by default
            if (url.getIpAddressTokens() <= 0)
                url.setIpAddressTokens(60);

            // set the tokens for the shortKey to 50,000 per-minute by default
            if (url.getUrlTokens() <= 0)
                url.setUrlTokens(50000);

            baseService.addUrlInDb(url);
            this.performCacheOperationsOnUrlCreate(user.getId(), url); // perform cache operations when url is
                                                                       // created

            return baseService.getUrlDto(url);
        } catch (Exception error) {
            System.out.println("Unexpected Error While Creating Short Url: " + error.getMessage());
            return new UrlDto();
        }
    }

    // method to update url by id
    public Map<String, UrlDto> updateUrlById(Urls url) {
        Map<String, UrlDto> response = new HashMap<>();
        if (url.getLongUrl() != null && !validateUrl(url)) {
            response.put("Invalid_url", new UrlDto());
            return response;
        } else if (url.getId() <= 0) {
            response.put("No_url", new UrlDto());
        }

        Users user = baseService.getAuthenticatedUser();
        Urls newUrl = baseService.getUrlByIdAndUser(url.getId(), user.getId());

        if (newUrl == null || newUrl.getShortKey().isBlank()) {
            response.put("No_url", new UrlDto());
            return response;
        }

        if (url.getLongUrl() != null)
            newUrl.setLongUrl(url.getLongUrl()); // set the new Url in the Entity

        // to set the default rateLimit if not set by the user
        if (url.getIpAddressTokens() > 0)
            newUrl.setIpAddressTokens(url.getIpAddressTokens());

        if (url.getUrlTokens() > 0)
            newUrl.setUrlTokens(url.getUrlTokens());

        baseService.updateUrlById(newUrl);
        UrlDto urlDto = baseService.getUrlDto(newUrl);

        // perform cache operations when an url is updated
        this.performCacheOperationsOnUrlUpdate(user.getId(), urlDto, url, newUrl);

        response.put("updated", urlDto);
        return response;
    }

    // method to delete url by Id
    public String deleteUrlById(long id) {
        if (id == 0)
            return "invalid_id";
        Users user = baseService.getAuthenticatedUser();
        Urls url = baseService.getUrlByIdAndUser(id, user.getId());

        if (url == null || url.getShortKey().isBlank()) {
            return "invalid_id";
        }

        if (baseService.deleteUrlByEntity(url)) {
            // perform url delete cache operations
            this.performCacheOperationsOnUrlDelete(url);
            return "deleted";
        } else {
            return "unexpected_error";
        }
    }
}
