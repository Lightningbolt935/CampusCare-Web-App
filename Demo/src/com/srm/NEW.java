package com.srm;

import java.sql.*;

public class NEW {
    public static void main(String args[]) {
        try {
  
            Class.forName("com.mysql.cj.jdbc.Driver");

     
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/res", "root", "tiger");

            Statement stmt = con.createStatement();

  
            String tbl = "CREATE TABLE IF NOT EXISTS Orders(" +
                         "order_id INT PRIMARY KEY," +
                         "customer_name VARCHAR(50)," +
                         "item VARCHAR(50)," +
                         "amount DECIMAL(8,2)," +
                         "status VARCHAR(20)," +
                         "quantity INT)";
            stmt.executeUpdate(tbl);
            System.out.println("Table Created");


            stmt.executeUpdate("DELETE FROM Orders");

 
            String ins1 = "INSERT INTO Orders VALUES(1, 'xyz', 'Momo', 50.00, 'Served')";
            String ins2 = "INSERT INTO Orders VALUES(2, 'abc', 'Burger', 80.00, 'Served')";
            stmt.executeUpdate(ins1);
            stmt.executeUpdate(ins2);
            System.out.println("Values Inserted");

            ResultSet rs = stmt.executeQuery("SELECT * FROM Orders");
            System.out.println("\nOrder Details:");
            System.out.println("id|name|item|amount|status");
            while (rs.next()) {
                int id = rs.getInt("order_id");
                String name = rs.getString("customer_name");
                String item = rs.getString("item");
                double amt = rs.getDouble("amount");
                String st = rs.getString("status");

                System.out.println(id +"|" + name + "|" + item + "|" + amt + "|" + st  );
            }

            String billQuery = "SELECT customer_name, SUM(amount) AS total " +
                               "FROM Orders " +
                               "WHERE status = 'Served' " +
                               "GROUP BY customer_name";

            ResultSet rs2 = stmt.executeQuery(billQuery);

            double grandTotal = 0.0;
            System.out.println("\nIndividual Bills:");
            while (rs2.next()) {
                String customer = rs2.getString("customer_name");
                double total = rs2.getDouble("total");
                System.out.println(customer + " bill: ₹" + total);
                grandTotal += total;
            }

            System.out.println("\nTotal bill of all served customers: ₹" + grandTotal);

            con.close();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}

