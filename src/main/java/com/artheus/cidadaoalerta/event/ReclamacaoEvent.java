package com.artheus.cidadaoalerta.event;

import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.enums.TipoEventoReclamacao;

/**
 * Evento publicado sempre que uma reclamação é criada ou seu status é alterado.
 */

public record ReclamacaoEvent(Reclamacao reclamacao, TipoEventoReclamacao tipoEvento) {
}
