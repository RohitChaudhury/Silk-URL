package Silk_Url.Silk_Url_Service.Config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@Configuration
@EnableCaching
public class CacheConfig { // Cache Configuration
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    @Value("${spring.data.redis.port:6379}")
    private String redisPort;

    // Create RedisClient Bean of the Bucket 4j Redis Lettuce
    @Bean
    public RedisClient redisClient() {
        RedisURI redisUri = RedisURI.builder().withHost(redisHost).withPort(Integer.parseInt(redisPort)).build();
        return RedisClient.create(redisUri);
    }

    @Bean
    public ProxyManager<String> proxyManager(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> redisConnection = redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        // Set TTL for the Bucket
        ExpirationAfterWriteStrategy expiration = ExpirationAfterWriteStrategy
                .basedOnTimeForRefillingBucketUpToMax(Duration.ofHours(2));

        ClientSideConfig clentConfig = ClientSideConfig.getDefault().withExpirationAfterWriteStrategy(expiration);

        // get the Proxymanager Bean
        return LettuceBasedProxyManager.builderFor(redisConnection).withClientSideConfig(clentConfig).build();
    }
}
