package Silk_Url.Silk_Url_Service.Model.Response;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Response {
    private String status;
    private int code;
    private String message;
    private ResponseError error;
    private List<?> data;

    public Response(ResponseError responseError) {
        this.error = responseError;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseError getError() {
        return error;
    }

    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }

    public void setResponseError(String type, List<ResponseErrorDetails> details) {
        error.setType(type);
        error.setDetails(details);
    }
}
