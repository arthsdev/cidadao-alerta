package com.artheus.cidadaoalerta.model;

import com.artheus.cidadaoalerta.validation.Latitude;
import com.artheus.cidadaoalerta.validation.Longitude;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Localizacao {

    @NotNull
    @Latitude // anotação customizada de latitude
    private Double latitude;

    @NotNull
    @Longitude
    private Double longitude;

}
