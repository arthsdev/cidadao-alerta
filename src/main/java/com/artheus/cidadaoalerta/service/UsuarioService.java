package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.domain.Usuario;
import com.artheus.cidadaoalerta.domain.enums.Role;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
import com.artheus.cidadaoalerta.mapper.UsuarioMapper;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public DetalhamentoUsuario cadastrarUsuario(CadastroUsuario cadastroDto) {
        Usuario usuario = usuarioMapper.toEntity(cadastroDto);
        usuario.setPapel(Role.ROLE_USER); // role padrão

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return usuarioMapper.toDetalhamentoDto(usuarioSalvo);
    }

    public List<DetalhamentoUsuario> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toDetalhamentoDto)
                .collect(Collectors.toList());
    }

    public DetalhamentoUsuario buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return usuarioMapper.toDetalhamentoDto(usuario);
    }

    public DetalhamentoUsuario atualizarUsuario(Long id, AtualizacaoUsuario dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza apenas os campos que vieram no DTO
        usuarioMapper.updateUsuarioFromDto(dto, usuario);

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        return usuarioMapper.toDetalhamentoDto(usuarioAtualizado);
    }


    public void desativarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}
