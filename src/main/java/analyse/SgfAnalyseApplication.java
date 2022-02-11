package analyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SgfAnalyseApplication {
    private static final Logger logger = LoggerFactory.getLogger(SgfAnalyseApplication.class);

    public static void main(String[] args) {
        logger.info("Start");
        SpringApplication.run(SgfAnalyseApplication.class, args);
    }

}
