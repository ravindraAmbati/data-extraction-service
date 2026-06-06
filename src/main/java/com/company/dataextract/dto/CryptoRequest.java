package com.company.dataextract.dto;

import javax.validation.constraints.NotBlank;

public class CryptoRequest {
    @NotBlank
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
