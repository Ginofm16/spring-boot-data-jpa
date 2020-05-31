package com.bolsadeideas.springboot.app.models.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

public interface IUploadFileService {

    /*mostra la imagen*/
    public Resource load(String filename) throws MalformedURLException;

    /*retorna String que seria el nombre cambiado de la imagen original, copia la nuevo
    directorio y la renombra con el nombre unico*/
    public String copy(MultipartFile file) throws IOException;

    public boolean delete(String filename);

    /*eliminar el directorio con todo imagenes*/
    public void deleteAll();
    //crear el directorio al arrancar el proyecto
    public void init () throws IOException;
}
