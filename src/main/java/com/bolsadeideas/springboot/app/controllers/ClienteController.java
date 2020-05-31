package com.bolsadeideas.springboot.app.controllers;

import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.models.service.IClienteService;
import com.bolsadeideas.springboot.app.models.service.IUploadFileService;
import com.bolsadeideas.springboot.app.util.paginator.PageRender;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

@Log4j2
@Controller
/*se puede quitar o comentar la linea de codigo la obtencio del id en modo Hidden de form.html
 * y podriamos usar @SessionAttributes mediante el cual se indica que se va guardar en los atributos
 * de la sesion del objeto cliente mapeado al formulario(clienteDao.findOne(id)) y lo pasa en la vista
 * y en la vista ya queda dentro de la sesion por lo tanto el id y todos sus datos quedan persistents
 * hasta que se envie al metodo guardar y en el metodo guarde eliminamos la sesion mediante SessionStatus*/
@SessionAttributes("cliente")
public class ClienteController {

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private IUploadFileService uploadFileService;

    /*:.+m expresion regular dentro del parametro de la url va permitir que spring no borre o trunque la
     * extencion del archivo, por cuando detecte la url termina en .jpg solo va captura el nombre del archivo y no
     * la extencion*/
    @GetMapping(value = "/uploads/{filename:.+}")
    //ResponseEntity es del tipo Resource porque se va retornar un recurso, una imagen a la respuesta http
    //se tiene que tomar filename, y convertirla a un Path para poder cargar esta imagen, un path absoluto
    public ResponseEntity<Resource> verFoto(@PathVariable String filename) {

        Resource recurso = null;
        try {
            recurso = uploadFileService.load(filename);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }

    @GetMapping(value = "/ver/{id}")
    public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

        Cliente cliente = clienteService.findOne(id);
        if (cliente == null) {
            flash.addFlashAttribute("error", "El cliente no existe en la base de datos");
            return "redirect:/listar";
        }

        model.put("cliente", cliente);
        model.put("titulo", "Detalle cliente: " + cliente.getNombre());

        return "ver";
    }

    /*
    @RequestMapping(value = "listar", method = RequestMethod.GET)
    public String listar(Model model){
        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", clienteService.findAll());
        return "listar";
    }
    */
    @RequestMapping(value = "listar", method = RequestMethod.GET)
    public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {

        Pageable pageRequest = PageRequest.of(page, 5);
        Page<Cliente> clientes = clienteService.findAll(pageRequest);

        PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);

        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", clientes);
        model.addAttribute("page", pageRender);

        return "listar";
    }

    /*1era fase el mostrar el formulario al usuario. Lo que se hace aca es crear una instancia de
     * un objeto Cliente y se lo pasa la vista*/
    @RequestMapping(value = "/form")
    public String crear(Map<String, Object> model) {

        Cliente cliente = new Cliente();
        model.put("cliente", cliente);
        model.put("titulo", "Formulario de Cliente");

        return "form";
    }

    @RequestMapping(value = "/form/{id}")
    public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

        Cliente cliente = null;

        if (id > 0) {
            cliente = clienteService.findOne(id);
            if (cliente == null) {
                flash.addFlashAttribute("error", "El ID del cliente no existe en la BBDD!");
                return "redirect:/listar";
            }
        } else {
            flash.addFlashAttribute("error", "El ID del cliente no puede ser cero!");
            return "redirect:/listar";
        }

        model.put("cliente", cliente);
        model.put("titulo", "Editar cliente");
        return "form";
    }


    /*2da fase es cuando el usuarioo envia en el submit los datos del formulario, se tiene que tener
     * un metodo que se encarge de procesar esos datos*/
    /*@Valid de javax para que recoja la validacion. Tener en cuenta que siempre BindingResult va
     * junto al objeto del formulario en este caso el Cliente; ya despues otros parametros*/
    /*@ModelAttribute("cliente"), permite colocar el nombre especificado en el model.put que trae al objeto
     * si en caso ese nombre es distinto al de la clase(en el parametro) sin tener en cuenta la mayuscula*/
    /*RedirectAttributes, va permitir mostart un mensaje se estado de un request al finalizarlo*/
    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
                          @RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Formulario de Cliente");
            return "form";
        }

        if (!foto.isEmpty()) {

            if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null && cliente.getFoto().length() > 0) {

                uploadFileService.delete(cliente.getFoto());
            }

            String uniqueFilename = null;
            try {
                uniqueFilename = uploadFileService.copy(foto);
            } catch (IOException e) {
                e.printStackTrace();
            }

            flash.addFlashAttribute("info", "Imagen subido correctamente '" + uniqueFilename + "'");

            cliente.setFoto(uniqueFilename);
        }

        String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con éxito!" : "Cliente creado con éxito!";

        clienteService.save(cliente);
        status.setComplete();
        flash.addFlashAttribute("success", mensajeFlash);
        return "redirect:listar";
    }

    @RequestMapping(value = "/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
        if (id > 0) {

            Cliente cliente = clienteService.findOne(id);

            clienteService.delete(id);
            flash.addFlashAttribute("success", "Cliente eliminado con éxito");

            if (uploadFileService.delete(cliente.getFoto())) {
                flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con exito");
            }

        }
        return "redirect:/listar";
    }
}
