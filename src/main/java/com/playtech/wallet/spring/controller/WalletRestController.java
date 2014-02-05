package com.playtech.wallet.spring.controller;

import com.playtech.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/wallet")
public class WalletRestController {

    @Autowired
    public WalletRestController(WalletService walletService) {
        this.walletService = walletService;
    }

    private WalletService walletService;

    @RequestMapping(value = "", method = RequestMethod.POST, produces = {"application/json", "application/xml"})
    public HttpEntity<WalletChangeResult> modifyWallet(@RequestBody WalletChangeMessage walletChangeMessage) {
        return new ResponseEntity<WalletChangeResult>(walletService.changeBalance(walletChangeMessage), HttpStatus.OK);
    }

}
