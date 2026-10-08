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

    /**
     * Esto funcion retorna una lista de producto, mediante el unmarshall del fichero xml
     *
     * @param fileXml
     * @return List<Producto>
     * @throws JAXBException
     */
    @Override
    public List<Producto> getProductos(String fileXml) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Productos.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Productos productos = (Productos) unmarshaller.unmarshal(new File(fileXml));
        return productos.getProducto();
    }

    /**
     * Esta funcion lo que hace es retornar una Lista de ProductoEntity mediante el uso de la funcion de getProducto
     * para que nos pase la lista de Producto y calcularemos con los valores del producto el precioFinal, coste, etc
     * y mediante esos datos haremos una instancia de ProductoEntity y lo pasaremos a la lista que devolvemos con esta funcion
     *
     * @param fileXml
     * @return
     * @throws JAXBException
     */
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

    /**
     * Con estas funcion devolveremos los datos a meter en el .txt por la pantalla y crearemos el punto txt con los
     * datos requeridos de la entidad SummaryEntity a partir de utilizar la funcion de getProductos
     *
     * @param path
     * @param fileXml
     * @return String
     * @throws JAXBException
     * @throws IOException
     */
    @Override
    public String getSummaryResume(String path, String fileXml) throws JAXBException, IOException {
        try {
            //Creamos un StringBuilder para mostrar luego los datos por la pantalla (no se si esto hacia falta)
            StringBuilder resumen = new StringBuilder();

            //creacion de la carpeta faltante(export)
            File directory = new File(path);
            directory.getAbsoluteFile().mkdirs();

            //Para saber cuantos productos tenemos
            int contador = 0;
            //Sacar beneficio total
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
            //Exportar los datos al .txt
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

    /**
     * Creacion de documento de excel apartir de un XML
     *
     * @param path
     * @param fileXml
     * @throws JAXBException
     * @throws IOException
     * @throws ParseException
     */
    public void exportarExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        try {
            int contador = 0;
            StringBuilder nombreFichero = new StringBuilder();
            nombreFichero.append("export_").append(fileXml.substring(fileXml.indexOf("_") + 1, fileXml.lastIndexOf("."))).append(".xlsx");
            File rute = new File(path, nombreFichero.toString());

            //Creacion del libro
            XSSFWorkbook factura = new XSSFWorkbook();
            //Creacion de la hoja
            XSSFSheet hoja = factura.createSheet("Factura");
            //Creacion de las filas
            XSSFRow fila = hoja.createRow(0);
            //Creacion de la celda
            XSSFCell celda = fila.createCell(0);
            XSSFCell celda1 = fila.createCell(1);
            XSSFCell celda2 = fila.createCell(2);
            XSSFCell celda3 = fila.createCell(3);
            XSSFCell celda4 = fila.createCell(4);
            XSSFCell celda5 = fila.createCell(5);
            XSSFCell celda6 = fila.createCell(6);
            XSSFCell celda7 = fila.createCell(7);


            //Configuracion de estilos 1
            XSSFCellStyle estilo = factura.createCellStyle();
            XSSFFont letras = factura.createFont();

            letras.setBold(true);

            estilo.setFont(letras);
            estilo.setBorderRight(BorderStyle.THICK);
            estilo.setBorderBottom(BorderStyle.THICK);
            estilo.setBorderLeft(BorderStyle.THICK);
            estilo.setBorderTop(BorderStyle.THICK);

            estilo.setBottomBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo.setRightBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo.setTopBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo.setLeftBorderColor(IndexedColors.DARK_GREEN.getIndex());

            //Configuracion de estilos 2
            XSSFCellStyle estilo2 = factura.createCellStyle();

            estilo2.setBorderRight(BorderStyle.THICK);
            estilo2.setBorderBottom(BorderStyle.THICK);
            estilo2.setBorderLeft(BorderStyle.THICK);
            estilo2.setBorderTop(BorderStyle.THICK);

            estilo2.setBottomBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo2.setRightBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo2.setTopBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo2.setLeftBorderColor(IndexedColors.DARK_GREEN.getIndex());

            estilo2.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            estilo2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            //Configuracion de estilo 3
            XSSFCellStyle estilo3 = factura.createCellStyle();
            estilo3.setFont(letras);

            estilo3.setBorderRight(BorderStyle.THICK);
            estilo3.setBorderBottom(BorderStyle.THICK);
            estilo3.setBorderLeft(BorderStyle.THICK);
            estilo3.setBorderTop(BorderStyle.THICK);

            estilo3.setBottomBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo3.setRightBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo3.setTopBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo3.setLeftBorderColor(IndexedColors.DARK_GREEN.getIndex());

            estilo3.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            estilo3.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            //Configuracion de estilos 4
            XSSFCellStyle estilo4 = factura.createCellStyle();

            estilo4.setBorderRight(BorderStyle.THICK);
            estilo4.setBorderBottom(BorderStyle.THICK);
            estilo4.setBorderLeft(BorderStyle.THICK);
            estilo4.setBorderTop(BorderStyle.THICK);

            estilo4.setBottomBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo4.setRightBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo4.setTopBorderColor(IndexedColors.DARK_GREEN.getIndex());
            estilo4.setLeftBorderColor(IndexedColors.DARK_GREEN.getIndex());

            //Pongo por defecto estas celdas en la fila 0 con sus estilos
            celda.setCellValue("Codigo");
            celda.setCellStyle(estilo);
            celda1.setCellValue("Numero de Serie");
            celda1.setCellStyle(estilo);
            celda2.setCellValue("Precio");
            celda2.setCellStyle(estilo);
            celda3.setCellValue("Descuento");
            celda3.setCellStyle(estilo);
            celda4.setCellValue("Precio Final");
            celda4.setCellStyle(estilo);
            celda5.setCellValue("Coste envio");
            celda5.setCellStyle(estilo);
            celda6.setCellValue("Coste almacenaje");
            celda6.setCellStyle(estilo);
            celda7.setCellValue("Precio");
            celda7.setCellStyle(estilo);

            List<ProductoEntity> producto = getDatos(fileXml);

            for (ProductoEntity productoEntity : producto) {
                contador++;
                Row filas = hoja.createRow(contador);

                Cell codigo = filas.createCell(0);
                Cell numeroS = filas.createCell(1);
                Cell precio = filas.createCell(2);
                Cell descuento = filas.createCell(3);
                Cell precioF = filas.createCell(4);
                Cell costeEnvio = filas.createCell(5);
                Cell costeAlamcenaje = filas.createCell(6);
                Cell beneficio = filas.createCell(7);

                //Y dependiendo de si es par o impar tenga color en el fondo de verde o de blanco
                if (contador % 2 == 0) {
                    codigo.setCellValue(productoEntity.getProducto().getCodigo());
                    codigo.setCellStyle(estilo);
                    numeroS.setCellValue(productoEntity.getProducto().getNumeroSerie());
                    numeroS.setCellStyle(estilo4);
                    descuento.setCellValue(productoEntity.getProducto().getDescuento().doubleValue() + "%");
                    descuento.setCellStyle(estilo4);
                    precio.setCellValue(productoEntity.getProducto().getPrecio().doubleValue() + "€");
                    precio.setCellStyle(estilo4);
                    precioF.setCellValue(productoEntity.getPrecioFinal().doubleValue() + "€");
                    precioF.setCellStyle(estilo4);
                    costeEnvio.setCellValue(productoEntity.getProducto().getCostes().getCostesEnvio().doubleValue() + "€");
                    costeEnvio.setCellStyle(estilo4);
                    costeAlamcenaje.setCellValue(productoEntity.getProducto().getCostes().getCostesAlmacenaje().doubleValue() + "€");
                    costeAlamcenaje.setCellStyle(estilo4);
                    beneficio.setCellValue(productoEntity.getProfit().doubleValue() + "€");
                    beneficio.setCellStyle(estilo4);
                } else {
                    codigo.setCellValue(productoEntity.getProducto().getCodigo());
                    codigo.setCellStyle(estilo3);
                    numeroS.setCellValue(productoEntity.getProducto().getNumeroSerie());
                    numeroS.setCellStyle(estilo2);
                    descuento.setCellValue(productoEntity.getProducto().getDescuento().doubleValue() + "%");
                    descuento.setCellStyle(estilo2);
                    precio.setCellValue(productoEntity.getProducto().getPrecio().doubleValue() + "€");
                    precio.setCellStyle(estilo2);
                    precioF.setCellValue(productoEntity.getPrecioFinal().doubleValue() + "€");
                    precioF.setCellStyle(estilo2);
                    costeEnvio.setCellValue(productoEntity.getProducto().getCostes().getCostesEnvio().doubleValue() + "€");
                    costeEnvio.setCellStyle(estilo2);
                    costeAlamcenaje.setCellValue(productoEntity.getProducto().getCostes().getCostesAlmacenaje().doubleValue() + "€");
                    costeAlamcenaje.setCellStyle(estilo2);
                    beneficio.setCellValue(productoEntity.getProfit().doubleValue() + "€");
                    beneficio.setCellStyle(estilo2);
                }
                hoja.autoSizeColumn(contador);
            }
            //exportar el .xlxs
            OutputStream outputStream = new FileOutputStream(rute);
            factura.write(outputStream);
            factura.close();
            outputStream.close();


        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
