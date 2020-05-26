package com.bolsadeideas.springboot.app.models.dao;

import com.bolsadeideas.springboot.app.models.entity.Cliente;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class ClienteDaoImpl implements IClienteDao {

    /*@PersistenceContext, contiene la unidad de persistencia, de forma automatica le va inyectar el entityManager
    * segun la configuracion de la unidad de persistencia que contiene el dataSource, que contiene el proveedor
    * JPA por defecto si no configuramos ningun tipo de base de datos de forma automatica springboot utiliza bd H2*/
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Cliente> findAll() {
        return em.createQuery("from Cliente").getResultList();
    }

    @Override
    public Cliente findOne(Long id) {
        return em.find(Cliente.class, id);
    }

    @Override
    public void save(Cliente cliente) {
    	if(cliente.getId() != null && cliente.getId() > 0) {
    		em.merge(cliente);
    	} else {
    		em.persist(cliente);
    	}
    }

    @Override
    public void delete(Long id) {
        em.remove(findOne(id));
    }
}
