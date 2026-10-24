package project.gelo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Direciona a URL raiz para a página pública estática do GELO. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/index.html";
    }
}
