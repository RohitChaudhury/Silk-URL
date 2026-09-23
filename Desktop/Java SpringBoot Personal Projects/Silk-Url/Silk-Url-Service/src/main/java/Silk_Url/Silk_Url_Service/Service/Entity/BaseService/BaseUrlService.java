package Silk_Url.Silk_Url_Service.Service.Entity.BaseService;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unbrokendome.base62.Base62;

import Silk_Url.Silk_Url_Service.Model.Dto.UrlDto;
import Silk_Url.Silk_Url_Service.Model.Entity.Urls;
import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import Silk_Url.Silk_Url_Service.Repository.UrlRepository;

@Service
public class BaseUrlService {
    private UrlRepository urlRepo;

    public static final String CACHE_ACC_NAME = "urls";
    public static final String CACHE_GEN_NAME = "gen_urls";
    public static final String CACHE_BUCKET_NAME = "urls_bucket";

    @Value("${server.baseUrl}")
    private String baseUrl;

    public BaseUrlService(UrlRepository urlRepo) {
        this.urlRepo = urlRepo;
    }

    // method to find User object of the Authenticated
    public Users getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users user = (Users) auth.getPrincipal();
        return user;
    }

    // method to update or Create Cache
    @CachePut(cacheNames = CACHE_ACC_NAME, key = "#shortKey")
    public String updateUrlAccessCache(String shortKey, String longUrl) {
        return longUrl;
    }

    // method to remove url from cache
    @CacheEvict(cacheNames = CACHE_ACC_NAME, key = "#shortKey")
    public void removeUrlAccessCache(String shortKey) {
    }

    // method to update url generate cache on update
    @CachePut(cacheNames = CACHE_GEN_NAME, key = "#userId + ':' + #id")
    public UrlDto updateUrlGenerateUpdateCache(long id, long userId, UrlDto urlDto) {
        return urlDto;
    }

    // method to delete getAll Url Cache
    @CacheEvict(cacheNames = CACHE_GEN_NAME, key = "#userId")
    public void removeUrlGenerateAllUrlsCache(long userId) {
    }

    // method to remove Url from generate url cache
    @CacheEvict(cacheNames = CACHE_GEN_NAME, key = "#userId + ':' + #id")
    public void removeUrlGenerateUpdateCache(long id, long userId) {
    }

    // method to update Url limit Cache
    @CachePut(cacheNames = CACHE_ACC_NAME, key = "'urlTokens:' + #shortKey")
    public long[] updateUrlAccessUrlTokensCache(String shortKey, long[] tokens) {
        return tokens;
    }

    // method to remove url limit Cache
    @CacheEvict(cacheNames = CACHE_ACC_NAME, key = "'urlTokens:' + #shortKey")
    public void removeUrlAccessUrlTokensCache(String shortkey) {
    }

    // method to remove BucketConfiguration
    @CacheEvict(cacheNames = CACHE_ACC_NAME, key = "'limitBucket:' + #tokens")
    public void removeUrlAccessUrlBucketConfigCache(long tokens) {
    }

    // method to remove token Bucket from Cache
    @CacheEvict(cacheNames = CACHE_BUCKET_NAME, allEntries = true)
    public void removeBucketCache() {
    }

    // method to get List of UrlDto from Urls
    public List<UrlDto> getUrlDtoFromUrls(List<Urls> urlsList) {
        List<UrlDto> urlDtos = new ArrayList<>();
        for (Urls url : urlsList) {
            urlDtos.add(this.getUrlDto(url));
        }

        return urlDtos;
    }

    // Get all Urls of the user-id
    public List<Urls> getAllUsersUrls(long userId) {
        return urlRepo.getAllUsersUrls(userId);
    }

    // Get the Url from the id and the User id
    public Urls getUrlByIdAndUser(long id, long userId) {
        return urlRepo.findUrlByIdAndUser(id, userId);
    }

    // method to get urlObject by id
    public Urls getUrlById(long id) {
        return urlRepo.findUrlById(id);
    }

    // get the next Entity Id of the last entry in DB
    public long getCurrentEntityDbId() {
        Urls url = urlRepo.getLastRowData();
        long id = 1L;

        if (url != null && !url.getShortKey().isBlank()) {
            id = url.getId() + 1L;
        }

        return id;
    }

    // method to generate a short Url
    public String generateKey(long id) {
        return Base62.encode(id);
    }

    // Save data in Db
    public Urls addUrlInDb(Urls url) {
        return urlRepo.save(url);
    }

    // method to validate Native Url
    public boolean isValidNativeUrl(String urlString) {
        try {
            new URI(urlString).toURL();
            return true;
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException error) {
            return false;
        }
    }

    // method to return a new UrlDto from Urls object
    public UrlDto getUrlDto(Urls url) {
        return new UrlDto(url.getId(), url.getLongUrl(),
                this.baseUrl + url.getShortKey(), url.getIpAddressTokens(), url.getUrlTokens(), url.getUpdate_at());
    }

    // method to update url by id
    public Urls updateUrlById(Urls url) {
        return urlRepo.save(url);
    }

    // method to delete url from the db
    public boolean deleteUrlByEntity(Urls url) {
        try {
            urlRepo.delete(url);
            return true;
        } catch (Exception error) {
            System.out.println("Unexpected Server Error while deleting URL");
            return false;
        }
    }

    // method to return the id from the shortkey
    public long getIdFromShortkey(String shortKey) {
        try {
            long[] decodedArray = Base62.decodeArray(shortKey);
            return decodedArray[0];
        } catch (Exception error) {
            System.out.println(error.getMessage());
            System.out.println("API called with Invalid Shortkey: " + shortKey);
            return 0L;
        }
    }

    // method to get Url Limit from ShortKey
    @Cacheable(cacheNames = CACHE_ACC_NAME, key = "'urlTokens:' + #shortKey")
    public long[] getTokensFromShortkey(String shortKey) {
        long id = this.getIdFromShortkey(shortKey);
        Urls urls = this.getUrlById(id);
        long[] tokens = { urls.getIpAddressTokens(), urls.getUrlTokens() };
        return tokens;
    }
}
