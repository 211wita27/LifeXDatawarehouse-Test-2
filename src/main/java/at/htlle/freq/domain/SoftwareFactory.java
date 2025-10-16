package at.htlle.freq.domain;

import org.springframework.stereotype.Component;

@Component
public class SoftwareFactory {
    public Software create(String name, String release, String revision, String supportPhase,
                           String licenseModel, String eos, String supportStart, String supportEnd) {
        return new Software(null, name, release, revision, supportPhase, licenseModel, eos, supportStart, supportEnd);
    }
}
