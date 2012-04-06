package org.platformlayer.guice.xaas;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "item_tags")
public class TagEntity {
    @Id
    public int id;

    @Column
    public int item;

    @Column
    public String key;

    @Column
    public String data;
}
