package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioNaoEncontradoException;
import com.artheus.cidadaoalerta.mapper.UsuarioMapper;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public DetalhamentoUsuario cadastrarUsuario(CadastroUsuario cadastroDto) {
        Usuario usuario = usuarioMapper.toEntity(cadastroDto);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setPapel(Role.ROLE_USER);

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
        Usuario usuario = buscarUsuarioPorId(id);
        return usuarioMapper.toDetalhamentoDto(usuario);
    }

    public DetalhamentoUsuario atualizarUsuario(Long id, AtualizacaoUsuario dto) {
        Usuario usuario = buscarUsuarioPorId(id);

        // Atualiza campos opcionais via Mapper
        usuarioMapper.updateUsuarioFromDto(dto, usuario);

        // Só re-hash se a senha for fornecida
        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDetalhamentoDto(usuarioAtualizado);
    }

    public void desativarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public DetalhamentoUsuario buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(email));
        return usuarioMapper.toDetalhamentoDto(usuario);
    }

    // ================= MÉTODO AUXILIAR PRIVADO =================
    private Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);
    }
}
