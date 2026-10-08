package org.educa.dao;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class ProductoDAOInterface implements ProductoDAO {
    @Override
    public List<Producto> getProductos(String fileXml) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Productos.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Productos productos = (Productos) unmarshaller.unmarshal(new File(fileXml));
        return productos.getProducto();
    }

    @Override
    public List<ProductoEntity> getDatos(String fileXml) throws JAXBException {

        List<ProductoEntity> productoEntities = new ArrayList<>();
        for (Producto p : getProductos(fileXml)) {
            //para antes de sacar el precio final lo que haremos es sacar el precio y el descuento
            BigDecimal precio = p.getPrecio();
            BigDecimal descuento = p.getDescuento();

            BigDecimal precioFinal = precio.subtract(precio.multiply(descuento).divide(BigDecimal.valueOf(100)))
                    .setScale(2, RoundingMode.HALF_UP);

            //añadimos los costes de envio y de almacenamiento en la variable coste
            BigDecimal coste = p.getCostes().getCostesEnvio().add(p.getCostes().getCostesAlmacenaje());

            ProductoEntity entidad = new ProductoEntity();
            entidad.setProducto(p);
            entidad.setPrecioFinal(precioFinal);
            entidad.setCost(coste);
            entidad.setProfit(precioFinal.subtract(coste));

            productoEntities.add(entidad);
        }
        return productoEntities;
    }
}
