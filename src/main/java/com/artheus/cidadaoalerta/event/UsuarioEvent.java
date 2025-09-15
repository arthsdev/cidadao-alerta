package com.artheus.cidadaoalerta.event;

import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.TipoEventoUsuario;

public record UsuarioEvent(Usuario usuario, TipoEventoUsuario tipoEvento) {}
