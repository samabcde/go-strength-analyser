package analyse;

import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log
public class SgfAnalyseApplication {

    public static void main(String[] args) {
        log.info("Start");
        SpringApplication.run(SgfAnalyseApplication.class, args);
    }

}
