package pl.lodz.p.it.expenseTracker.repository.tracking;

import org.springframework.data.repository.CrudRepository;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;

import java.util.Optional;

public interface GroupRepository extends CrudRepository<Group, Long> {
    Optional<Group> findGroupById(Long id);

    Optional<Group> findGroupByName(String name);


}
