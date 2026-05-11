package com.renewable.ai.util;

import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class ExportUtil {
    
    public static <T> void exportToCSV(HttpServletResponse response, String filename, 
                                       List<String> headers, List<T> data, 
                                       ICsvDataMapper<T> mapper) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".csv\"");
        
        try (Writer writer = response.getWriter();
             CSVWriter csvWriter = new CSVWriter(writer)) {
            
            csvWriter.writeNext(headers.toArray(new String[0]));
            
            for (T item : data) {
                String[] row = mapper.mapToRow(item);
                csvWriter.writeNext(row);
            }
            
            csvWriter.flush();
        }
    }
    
    public interface ICsvDataMapper<T> {
        String[] mapToRow(T item);
    }
}
