package com.bolsadeideas.springboot.app;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Log4j2
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /*permite agregar directiorio de recursos externos a nuestro proyecto*/
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);

        /*"uploads", se crea y registra un directorio*/
        /*.toAbsolutePath(), para qye sea una ruta absoluta. toUri(), para que incluya el esquema file:/, loq ue
        * se busca es algo como: "file:/D:/WorkSpace/Spring5/spring-boot-data-jpa/uploads"*/
        String resourcePath = Paths.get("uploads").toAbsolutePath().toUri().toString();
        log.info(resourcePath);
        /*ese directorio esta mapeado a una url, /uploads/*/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);

    }
}
