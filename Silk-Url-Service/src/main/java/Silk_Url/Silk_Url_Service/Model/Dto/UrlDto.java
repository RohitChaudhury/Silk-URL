package Silk_Url.Silk_Url_Service.Model.Dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UrlDto implements Serializable {
    private long id;
    private String longUrl;
    private String shortUrl;
    private long ipAddressTokens;
    private long urlTokens;
    private LocalDateTime lastChanged;

    public UrlDto() {
    }

    public UrlDto(long id, String longUrl, String shortUrl, long ipAddressTokens, long urlTokens,
            LocalDateTime lastChanged) {
        this.id = id;
        this.longUrl = longUrl;
        this.shortUrl = shortUrl;
        this.lastChanged = lastChanged;
        this.ipAddressTokens = ipAddressTokens;
        this.urlTokens = urlTokens;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public long getIpAddressTokens() {
        return ipAddressTokens;
    }

    public void setIpAddressTokens(long ipAddressTokens) {
        this.ipAddressTokens = ipAddressTokens;
    }

    public long getUrlTokens() {
        return urlTokens;
    }

    public void setUrlTokens(long urlTokens) {
        this.urlTokens = urlTokens;
    }

    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(LocalDateTime lastChanged) {
        this.lastChanged = lastChanged;
    }
}
