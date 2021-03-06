package won.protocol.repository;

import won.protocol.model.Need;
import won.protocol.model.OwnerApplication;

import java.net.URI;
import java.util.List;

/**
 * User: sbyim
 * Date: 11.11.13
 */
public interface OwnerApplicationRepository extends WonRepository<OwnerApplication>{
    List<OwnerApplication> findByOwnerApplicationId(String ownerApplicationId);
}
