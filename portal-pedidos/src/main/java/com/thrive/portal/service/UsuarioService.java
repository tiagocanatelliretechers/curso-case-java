package com.thrive.portal.service;

import com.thrive.portal.domain.Usuario;
import com.thrive.portal.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * A02 - hashing de senha inaceitavel: MD5 sem salt e sem work factor.
     * Sera migrado para BCrypt no Lab 3.3.
     */
    public String hash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(senha.getBytes());
            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean senhaConfere(String senhaDigitada, String hashArmazenado) {
        return hash(senhaDigitada).equals(hashArmazenado);
    }

    public Usuario registrar(String email, String senha, String role, Long clienteId) {
        Usuario u = new Usuario();
        u.setEmail(email);
        // A07 - sem politica de senha (aceita qualquer coisa, inclusive "123")
        u.setSenha(hash(senha));
        u.setRole(role);
        u.setClienteId(clienteId);
        return usuarioRepository.save(u);
    }

    public Optional<Usuario> porEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * A07 - fluxo de "esqueci minha senha" com user enumeration:
     * retorna mensagem diferente quando o e-mail existe vs. nao existe,
     * permitindo ao atacante descobrir contas validas.
     */
    public String recuperarSenha(String email) {
        Optional<Usuario> u = usuarioRepository.findByEmail(email);
        if (u.isPresent()) {
            return "Enviamos um link de redefinicao para " + email;
        }
        return "E-mail " + email + " nao encontrado em nossa base.";
    }
}
