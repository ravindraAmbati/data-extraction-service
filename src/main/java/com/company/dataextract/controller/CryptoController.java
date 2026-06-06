package com.company.dataextract.controller;

import com.company.dataextract.dto.CryptoRequest;
import com.company.dataextract.dto.CryptoResponse;
import com.company.dataextract.util.PasswordCryptoUtil;
import io.swagger.v3.oas.annotations.Operation;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class CryptoController {
    private final PasswordCryptoUtil passwordCryptoUtil;

    public CryptoController(PasswordCryptoUtil passwordCryptoUtil) {
        this.passwordCryptoUtil = passwordCryptoUtil;
    }

    @PostMapping({"/encrypt", "/api/crypto/encrypt"})
    @Operation(summary = "Encrypt a plain text value")
    public CryptoResponse encrypt(@Valid @RequestBody CryptoRequest request) {
        return new CryptoResponse(passwordCryptoUtil.encrypt(request.getValue()));
    }

    @PostMapping({"/decrypt", "/api/crypto/decrypt"})
    @Operation(summary = "Decrypt an encrypted value")
    public CryptoResponse decrypt(@Valid @RequestBody CryptoRequest request) {
        return new CryptoResponse(passwordCryptoUtil.decrypt(request.getValue()));
    }
}
