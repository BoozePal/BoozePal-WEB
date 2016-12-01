package hu.deik.boozepal.common.vo;

import hu.deik.boozepal.common.entity.DrinkTypeEnum;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
public class PubVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * A kocsma azonosítója.
     */
    private Long id;

    /**
     * Kocsma megnevezése.
     */
    private String name;

    /**
     * Város megnevezése.
     */
    private String town;

}
