package com.bolsadeideas.springboot.app.models.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Log4j2
@Service
public class UploadFileServiceImpl implements IUploadFileService {

    //uploads, seria la carpeta
    private final static String UPLOAD_FOLDER = "uploads";

    @Override
    public Resource load(String filename) throws MalformedURLException {

        Path pathFoto = getPath(filename);
        log.info("pathFoto: " + pathFoto);
        Resource recurso = null;

        //UrlResource permte cargar la imagen en la respuesta HTTP
        recurso = new UrlResource(pathFoto.toUri());
        if (!recurso.exists() && !recurso.isReadable()) {
            throw new RuntimeException("Error: no se puede cargar la imagen: " + pathFoto.toString());
        }

        return recurso;
    }

    @Override
    public String copy(MultipartFile file) throws IOException {
        // Codigo para evitar tener archivos con el mismo nombre
        String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        // creando una una ruta absoluta en el directorio raiz dentro del proyecto
        //la ruta seria: uploads/(el nombre del archivo)
        Path rootPath = getPath(uniqueFilename);

        log.info("rootPath: " + rootPath);

        Files.copy(file.getInputStream(), rootPath);

        return uniqueFilename;
    }

    @Override
    public boolean delete(String filename) {
        Path rootPath = getPath(filename);
        File archivo = rootPath.toFile();
        if (archivo.exists() && archivo.canRead()){
            if (archivo.delete()){
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(Paths.get(UPLOAD_FOLDER).toFile());
    }

    @Override
    public void init() throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_FOLDER));
    }

    public Path getPath(String filename) {
        /*.get("uploads"), directorio raiz. resolve, permite concatenar otro path al path principal.
         * toAbsolutePath(), para obyener la ruta completa, desde: D:/workspace/Spring5/....*/
        return Paths.get(UPLOAD_FOLDER).resolve(filename).toAbsolutePath();
    }
}
