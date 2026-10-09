package com.studentmanagement.blob;

import java.io.*;
import java.sql.*;


import com.studentmanagement.util.DBConnection;

public class BlobDemo{

    private static final String INSERT_QUERY =
            "INSERT INTO student_documents "+
            "(student_id,file_name, file_type, file_data) "+
            "VALUES(?,?,?,?)";

    private static final String SELECT_sql =
            "SELECT file_name, file_type, file_data " +
            "from student_documents "+
            "where document_id = ?";

    private static void insertBlob(int studentId, String filePath){

        try(Connection connection = DBConnection.getConnection();
            PreparedStatement prepareStatement= connection.prepareStatement(INSERT_QUERY);
            FileInputStream fis = new FileInputStream(filePath);
        )
        {
            prepareStatement.setInt(1, studentId);
            prepareStatement.setString(2, new File(filePath).getName());
            prepareStatement.setString(3,"application/octet-stream");
            prepareStatement.setBinaryStream(4,fis);

            int rowsAffected = prepareStatement.executeUpdate();
            System.out.println("Document inserted successfully. Rows affected: " + rowsAffected);
        }
        catch(SQLException s){
            s.printStackTrace();
        }
        catch(IOException io){
            io.printStackTrace();
        }

    }



    private static void retrieveBlob(int documentId, String outputPath){
        try(Connection connection  = DBConnection.getConnection();
            PreparedStatement prepareStatement = connection.prepareStatement(SELECT_sql);)
        {
            prepareStatement.setInt(1, documentId);

            try(ResultSet resultSet = prepareStatement.executeQuery();){

                if(resultSet.next()){

                    Blob blob = resultSet.getBlob("file_data");

                    try(InputStream is = blob.getBinaryStream();

                        FileOutputStream fos = new FileOutputStream(outputPath);){

                        byte []  buffer = new byte[8192];

                        int byteRead;

                        while((byteRead = is.read(buffer)) != -1){

                            fos.write(buffer, 0, byteRead);
                        }
                    }
                    System.out.println("Document retrieved successfully "+ outputPath);
                }
                else{
                    System.out.println("No document found with that ID: "+ documentId);
                }
            }
        }
        catch(SQLException | IOException f){
            f.printStackTrace();
        }

    }


    public static void main(String[] args) {

        int studentId = 1;
        String filePath = "C:\\Users\\nbmop\\OneDrive\\Documents\\Dinesh_Karri-1.pdf";

        insertBlob(studentId, filePath);

        retrieveBlob(3, "C:\\Users\\nbmop\\OneDrive\\Documents\\Recovered_Dinesh.pdf");
    }

}
