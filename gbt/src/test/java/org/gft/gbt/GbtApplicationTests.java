package org.gft.gbt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.gft.gbt.config.TestMongoConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMongoConfig.class)
class GbtApplicationTests {


}
