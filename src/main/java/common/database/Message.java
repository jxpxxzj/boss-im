package common.database;

import common.enums.EndpointType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    public long sender;
    public long container;
    public EndpointType containerType;
    public long target;
    public EndpointType targetType;

    public long timestamp;
    public boolean useEscape;
    public String content;
    public boolean hasRead;
}
