package org.educa.dao;

import generated.Producto;
import jakarta.xml.bind.JAXBException;
import org.educa.entity.ProductoEntity;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface ProductoDAO {

    List<Producto> getProductos(String pathXML) throws JAXBException;
    List<ProductoEntity> getDatos(String fileXml) throws JAXBException;
    String getSummaryResume(String path, String fileXml) throws JAXBException, IOException;
    void exportarExcel(String path, String fileXml) throws JAXBException, IOException, ParseException;

}
