package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.entity.ProductoEntity;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;


public class ProductoService {

    private final ProductoDAO productoDAO = new ProductoDAOInterface();

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {

    }
    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {

    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
    }
}
