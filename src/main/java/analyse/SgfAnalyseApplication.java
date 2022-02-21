package analyse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SgfAnalyseApplication {

    public static void main(String[] args) {
        log.info("Start");
        SpringApplication.run(SgfAnalyseApplication.class, args);
    }

}
