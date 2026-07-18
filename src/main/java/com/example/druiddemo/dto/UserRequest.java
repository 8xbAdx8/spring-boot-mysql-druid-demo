package com.example.druiddemo.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(max = 64, message = "姓名不能超过 64 个字符")
        String name,

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱不能超过 128 个字符")
        String email,

        @Pattern(regexp = "ACTIVE|DISABLED", message = "状态只能是 ACTIVE 或 DISABLED")
        String status
) {
    public String normalizedStatus() {
        return status == null || status.isBlank() ? "ACTIVE" : status;
    }
}
