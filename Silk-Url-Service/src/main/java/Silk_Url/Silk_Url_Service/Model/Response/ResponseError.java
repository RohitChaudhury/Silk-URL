package Silk_Url.Silk_Url_Service.Model.Response;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ResponseError {
    private String type;
    private List<ResponseErrorDetails> details;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ResponseErrorDetails> getDetails() {
        return details;
    }

    public void setDetails(List<ResponseErrorDetails> details) {
        this.details = details;
    }
}
