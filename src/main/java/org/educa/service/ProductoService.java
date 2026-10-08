package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOInterface;
import org.educa.entity.ProductoEntity;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;


public class ProductoService {

    private final ProductoDAO productoDAO = new ProductoDAOInterface();

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        List<ProductoEntity> productoEntities = productoDAO.getDatos(fileXml);
        return productoEntities;
    }

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        System.out.println("Lo que vas a ggetuardar es lo siguiente: ");
        System.out.println(productoDAO.getSummaryResume(path, fileXml));
    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        productoDAO.exportarExcel(path, fileXml);
    }
}
