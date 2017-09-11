package common.database;

import common.enums.GroupMemberType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;
    public long groupId;
    public long userId;
    public GroupMemberType type;
}
