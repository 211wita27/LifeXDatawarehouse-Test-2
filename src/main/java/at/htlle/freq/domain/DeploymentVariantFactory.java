package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

@Component
public class DeploymentVariantFactory {
    public DeploymentVariant create(String code, String name, String description, boolean active) {
        return new DeploymentVariant(null, code, name, description, active);
    }
}
