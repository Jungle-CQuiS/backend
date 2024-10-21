package meowKai.CQuiS_backend.global.base;

public record ApiResponse<T>(
        T data,
        String message,
        String code
) {
    public static <T> ApiResponse<T> ofSuccess(T data) {
        return new ApiResponse<>(data, "성공", "S001");
    }

    public static <T> ApiResponse<T> ofSuccess() {
        return new ApiResponse<>(null, "성공", "S001");
    }
}