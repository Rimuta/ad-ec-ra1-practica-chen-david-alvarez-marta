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
    @Override
    public String getSummaryResume(String path, String fileXml) throws JAXBException, IOException {
        try {

            StringBuilder resumen = new StringBuilder();

            //creacion de la carpeta faltante(export)
            File directory = new File(path);
            directory.getAbsoluteFile().mkdirs();

            int contador = 0;
            BigDecimal beneficioT = new BigDecimal("0.00");

            for (Producto producto : getProductos(fileXml)) {
                BigDecimal precio = producto.getPrecio();
                BigDecimal descuento = producto.getDescuento();
                BigDecimal coste = producto.getCostes().getCostesEnvio().add(producto.getCostes().getCostesAlmacenaje());
                BigDecimal precioFinal = precio.subtract(precio.multiply(descuento).divide(BigDecimal.valueOf(100))).setScale(2, RoundingMode.HALF_UP);

                beneficioT = beneficioT.add(precioFinal.subtract(coste));
                contador++;
            }

            String date = fileXml.substring(fileXml.indexOf("_") + 1, fileXml.lastIndexOf("."));
            String nameWithOutExtension = "result_" + date;
            String nameWithExtension = "resul_" + date + ".txt";

            File xml = new File(fileXml);
            long fileSize = xml.length();

            File summary = new File(path, nameWithExtension);
            summary.createNewFile();

            SummaryEntity summaryEntity = new SummaryEntity(date, contador, beneficioT, fileXml, nameWithOutExtension, fileSize);

            resumen.append("Fecha: ").append(date).append("\n").append("NumeroDeProductos: ").append(contador).append("\n")
                    .append("BeneficioTotal: ").append(beneficioT).append("\n").append("Ruta del fichero: ").append(fileXml).append("\n")
                    .append("Nombre de fichero: ").append(nameWithOutExtension).append("\n").append("Tamano del fichero: ").append(fileSize);

            try (PrintWriter pw = new PrintWriter(new FileWriter(summary, true))) {
                pw.println(summaryEntity.toPrint());
                return resumen.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
