package pl.coffeechess.eureka.actuator;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Endpoint(id = "eureka-registry")
@RequiredArgsConstructor
public class EurekaRegistryEndpoint {

    private final PeerAwareInstanceRegistry registry;

    @ReadOperation
    public Map<String, Object> registry() {
        var applications = registry.getApplications().getRegisteredApplications();

        List<Map<String, Object>> services = applications.stream()
                .sorted(Comparator.comparing(app -> app.getName().toLowerCase()))
                .map(app -> {
                    Map<String, Object> service = new LinkedHashMap<>();
                    service.put("name", app.getName());
                    service.put("instanceCount", app.getInstances().size());
                    service.put("instances", app.getInstances().stream()
                            .map(this::toInstanceSummary)
                            .toList());
                    return service;
                })
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("registeredServiceCount", applications.size());
        response.put("registeredInstanceCount", applications.stream()
                .mapToInt(app -> app.getInstances().size())
                .sum());
        response.put("services", services);
        return response;
    }

    private Map<String, Object> toInstanceSummary(InstanceInfo instance) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("instanceId", instance.getInstanceId());
        summary.put("hostName", instance.getHostName());
        summary.put("status", instance.getStatus().name());
        summary.put("port", instance.getPort());
        summary.put("homePageUrl", instance.getHomePageUrl());
        summary.put("healthCheckUrl", instance.getHealthCheckUrl());
        return summary;
    }
}
