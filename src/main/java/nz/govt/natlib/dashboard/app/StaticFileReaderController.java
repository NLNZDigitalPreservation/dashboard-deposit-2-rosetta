package nz.govt.natlib.dashboard.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaticFileReaderController {
    @RequestMapping(value = {
            "/{path:[^.]*}",
            "/**/{path:[^.]*}",
            "/**/{path:^(?!index\\.html$).+\\.html$}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}