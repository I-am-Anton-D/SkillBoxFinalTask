package main.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Entity for global settings
 */
@Entity
public class GlobalSettings {

    /**
     * unique id in DB
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /**
     * system code of setting
     */
    @NotNull
    private String code;

    /**
     * name of setting
     */
    @NotNull
    private String name;

    /**
     * value of setting
     */
    @NotNull
    private boolean value;

    /**
     * Standard getter
     *
     * @return system code of setting
     */

    public String getCode() {
        return code;
    }

    /**
     * Standard setter set code of system setting
     */

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Standard getter
     *
     * @return name of setting
     */

    public String getName() {
        return name;
    }

    /**
     * Standard setter set the name of system setting
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Standard getter
     *
     * @return value of setting
     */

    public boolean getValue() {
        return value;
    }

    /**
     * Standard setter set the value of system setting
     */
    public void setValue(boolean value) {
        this.value = value;
    }
}
