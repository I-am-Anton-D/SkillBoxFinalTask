package main.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity for tags
 */

@Entity
@Table(name = "tags")
public class Tag {

    /**
     * unique id in DB
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /**
     * name of tage
     */

    @NotNull
    private String name;

    /**
     * empty constructor for Spring
     */

    public Tag() {
    }

    /**
     * constructor for creating entity
     */

    public Tag(String name) {
        this.name = name;
    }

    /**
     * Standard getter
     *
     * @return id of tag
     */

    public int getId() {
        return id;
    }

    /**
     * Standard getter
     *
     * @return name of tag
     */

    public String getName() {
        return name;
    }
}
