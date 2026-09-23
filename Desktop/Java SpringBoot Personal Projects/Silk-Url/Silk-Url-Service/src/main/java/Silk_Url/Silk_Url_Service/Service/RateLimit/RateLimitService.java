package Silk_Url.Silk_Url_Service.Service.RateLimit;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import Silk_Url.Silk_Url_Service.Service.Entity.BaseService.BaseUrlService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;

@Service
public class RateLimitService {
    private ProxyManager<String> proxyManager;

    public RateLimitService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    // method to to get the Bucket Configurations for tokens
    @Cacheable(cacheNames = BaseUrlService.CACHE_ACC_NAME, key = "'bucketToken:' + #limit")
    private Supplier<BucketConfiguration> getConfigSupplierForLimit(long limit) {
        Bandwidth bandwidth = Bandwidth.builder().capacity(limit).refillIntervally(limit, Duration.ofMinutes(1))
                .build();

        Supplier<BucketConfiguration> bucketConfig = () -> (BucketConfiguration.builder().addLimit(bandwidth).build());
        return bucketConfig;
    }

    // method to get the Bucket for IP and Shortkey
    public Bucket resolveBucketIpAddress(String ipAddress, String shortKey, long tokens) {
        Supplier<BucketConfiguration> configSupplier = this.getConfigSupplierForLimit(tokens);
        String bucketKey = BaseUrlService.CACHE_BUCKET_NAME + "::" + shortKey + ":" + ipAddress;
        return proxyManager.builder().build(bucketKey, configSupplier);
    }

    // method to get the Bucket for ShortKey
    public Bucket resolveBucketShortKey(String shortKey, long tokens) {
        Supplier<BucketConfiguration> configSupplier = this.getConfigSupplierForLimit(tokens);
        String bucketKey = BaseUrlService.CACHE_BUCKET_NAME + "::" + shortKey;
        return proxyManager.builder().build(bucketKey, configSupplier);
    }
}
