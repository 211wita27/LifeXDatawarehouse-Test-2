package at.htlle.freq.domain;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class ServiceContractFactory {
    public ServiceContract create(UUID accountID, UUID projectID, UUID siteID,
                                  String contractNumber, String status,
                                  String startDate, String endDate) {
        return new ServiceContract(null, accountID, projectID, siteID, contractNumber, status, startDate, endDate);
    }
}
