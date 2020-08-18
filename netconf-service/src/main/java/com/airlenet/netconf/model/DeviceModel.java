package com.airlenet.netconf.model;

//import com.airlenet.data.domain.Userable;
//import com.airlenet.data.jpa.BaseEntity;
//import com.airlenet.data.jpa.DataEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table
public class DeviceModel implements Serializable {
    @Id
    private Long id;
    private String name;
    private String ip;
    private int port;
    private String user;
    private String pass;
}
