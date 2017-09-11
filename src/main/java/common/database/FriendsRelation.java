package common.database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class FriendsRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    public long user1;
    public long user2;
    public long createTime;
}
