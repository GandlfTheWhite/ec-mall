package com.zyd.ecmall;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowSafetyTest {
    @Test
    void bothDeploymentsRequireVerificationMasterAndExplicitEnablement() throws Exception {
        for (String name : List.of("deploy-backend.yml", "deploy-frontend.yml")) {
            try (var input = Files.newInputStream(Path.of(".github", "workflows", name))) {
                Map<?, ?> workflow = new Yaml().load(input);
                Map<?, ?> jobs = (Map<?, ?>) workflow.get("jobs");
                Map<?, ?> deploy = (Map<?, ?>) jobs.get("deploy");
                assertEquals("verify", deploy.get("needs"));
                assertEquals("production", deploy.get("environment"));
                String condition = (String) deploy.get("if");
                assertTrue(condition.contains("refs/heads/master"));
                assertTrue(condition.contains("vars.PRODUCTION_DEPLOY_ENABLED == 'true'"));
                assertTrue(condition.contains("github.event_name == 'push'"));
                assertTrue(jobs.containsKey("verify"));
            }
        }
    }

    @Test
    void backendDoesNotSkipTestsAndChecksEbBeforeDeploying() throws Exception {
        try (var input = Files.newInputStream(Path.of(".github", "workflows", "deploy-backend.yml"))) {
            Map<?, ?> workflow = new Yaml().load(input);
            Map<?, ?> jobs = (Map<?, ?>) workflow.get("jobs");
            String verify = jobs.get("verify").toString();
            assertTrue(verify.contains("mvn --batch-mode verify"));
            assertFalse(verify.contains("skipTests"));
            Map<?, ?> deploy = (Map<?, ?>) jobs.get("deploy");
            List<?> steps = (List<?>) deploy.get("steps");
            String beforeDeployment = steps.get(steps.size() - 2).toString();
            assertTrue(beforeDeployment.contains("check_eb_environment.py"));
            assertTrue(steps.get(steps.size() - 1).toString().contains("target/ec-mall-eb.zip"));
            assertTrue(verify.contains("package_backend.py"));
            assertTrue(verify.contains("backend-bundle"));
            assertTrue(verify.contains("target/ec-mall-eb.zip"));
            assertTrue(Files.readString(Path.of(".platform/nginx/conf.d/20_upload_size.conf")).contains("client_max_body_size 6m;"));
            assertEquals("web: java -jar application.jar", Files.readString(Path.of("Procfile")).trim());
        }
    }
}
