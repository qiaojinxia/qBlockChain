package com.qjx.blockchain.qblockchain;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by caomaoboy 2019-11-08
 **/
@Controller
public class IndexController {
    @GetMapping("/")
    public String index() throws Exception {
        return "forward:index.html";
    }
}
