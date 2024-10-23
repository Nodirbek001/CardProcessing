package uz.ns.cardprocessing.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
public class ApiResult<T> implements Serializable {
    private boolean success;
    private T data;
    private List<ErrorData> errors;

    private ApiResult(Boolean success) {
        this.success = success;
    }

    private ApiResult(Boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    private ApiResult(String devMsg, String userMsg, Integer errorCode) {
        this.success = false;
        this.errors = Collections.singletonList(new ErrorData(devMsg, userMsg, errorCode));

    }

    private ApiResult(List<ErrorData> errors) {
        this.success = false;
        this.errors = errors;
    }

    public static <E> ApiResult<E> successResponse(E data, Integer errorCode) {
        return new ApiResult<>(true, data);
    }

    public static <E> ApiResult<E> successResponse() {
        return new ApiResult<>(true);
    }

    public static <E> ApiResult<E> errorResponse(String devMsg, String userMsg, Integer errorCode) {
        return new ApiResult<>(devMsg, userMsg, errorCode);
    }

}
