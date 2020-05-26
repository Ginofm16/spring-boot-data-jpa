package com.bolsadeideas.springboot.app.controllers;

import com.bolsadeideas.springboot.app.models.dao.IClienteDao;
import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.models.service.IClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import javax.validation.Valid;
import java.util.Map;

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

    @RequestMapping(value = "listar", method = RequestMethod.GET)
    public String listar(Model model){
        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", clienteService.findAll());
        return "listar";
    }

    /*1era fase el mostrar el formulario al usuario. Lo que se hace aca es crear una instancia de
     * un objeto Cliente y se lo pasa la vista*/
    @RequestMapping(value = "/form")
    public String crear(Map<String, Object> model){

        Cliente cliente = new Cliente();
        model.put("cliente", cliente);
        model.put("titulo","Formulario de Cliente");

        return "form";
    }
    
    @RequestMapping(value="/form/{id}")
    public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model) {
    	
    	Cliente cliente = null;
    	
    	if(id>0) {
    		cliente = clienteService.findOne(id);
    	} else {
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
    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String guardar(@Valid Cliente cliente, BindingResult result, Model model, SessionStatus status){
        if (result.hasErrors()){
            model.addAttribute("titulo", "Formulario de Cliente");
            return "form";
        }

        clienteService.save(cliente);
        status.setComplete();
        return "redirect:listar";
    }

    @RequestMapping(value = "/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Long id){
        if(id>0) {
            clienteService.delete(id);
        }
        return "redirect:/listar";
    }
}
