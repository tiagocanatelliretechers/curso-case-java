package com.thrive.portal.service;

import com.thrive.portal.domain.Comprovante;
import com.thrive.portal.repository.ComprovanteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploadService {

    private final ComprovanteRepository comprovanteRepository;
    private final String uploadDir;

    public UploadService(ComprovanteRepository comprovanteRepository,
                         @Value("${portal.upload.dir}") String uploadDir) {
        this.comprovanteRepository = comprovanteRepository;
        this.uploadDir = uploadDir;
    }

    /**
     * A04/A05 - upload irrestrito de arquivos.
     *
     * Problemas do baseline:
     *  - aceita QUALQUER extensao/content-type (permite .jsp, .sh, .exe...)
     *  - usa o nome de arquivo enviado pelo cliente (path traversal: ../../)
     *  - nao valida tamanho nem "magic number"
     *  - grava com o nome original
     *
     * Sera corrigido no Lab 2.4 (whitelist, magic number, limite, rename).
     */
    public Comprovante salvar(Long pedidoId, MultipartFile arquivo) throws IOException {
        String nomeOriginal = arquivo.getOriginalFilename();

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        // VULNERAVEL: concatena o nome enviado pelo cliente sem sanitizar.
        Path destino = dir.resolve(nomeOriginal);
        arquivo.transferTo(destino.toAbsolutePath());

        Comprovante c = new Comprovante();
        c.setPedidoId(pedidoId);
        c.setNomeArquivo(nomeOriginal);
        c.setCaminho(destino.toString());
        c.setContentType(arquivo.getContentType());
        c.setTamanho(arquivo.getSize());
        return comprovanteRepository.save(c);
    }
}
