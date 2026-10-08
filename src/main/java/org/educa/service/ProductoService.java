package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.entity.ProductoEntity;

import java.util.List;


public class ProductoService {

    private final ProductoDAO productoDAO = new ProductoDAOInterface();

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        List<ProductoEntity> productoEntities = productoDAO.getDatos(fileXml);
        return productoEntities;
    }

}
